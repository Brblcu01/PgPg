package be4fe.service;

import be4fe.dto.LoginRequestDTO;
import be4fe.dto.RefreshDTO;
import be4fe.dto.UserInfoDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import common.base.BaseDTO;
import common.base.BaseEntity;
import common.base.BaseGenericRestService;
import common.base.BaseRepository;
import common.dto.*;
import common.entity.*;
import common.mapper.ExternalAuthMapper;
import common.repository.*;
import common.utils.Utils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends BaseGenericRestService<BaseEntity, BaseDTO, BaseRepository<BaseEntity>>
		implements AuthService {

	private final ExternalAuthServiceImpl externalAuthService;
	private final CfUserRepository userRepository;
	private final RestTemplate restTemplate;
	private final ExternalAuthMapper externalAuthMapper;
	private final ExternalAuthRepository externalAuthRepository;
	private final CfRoleRepository cfRoleRepository;
	private final CfSystemConfigRepository cfSystemConfigRepository;
	private final CeOrganizationalStructureRepository ceOrganizationalStructureRepository;
	private final CeOfficeRepository ceOfficeRepository;

	@Value("${SSO}")
	private String ssoUrl;

	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> login(HttpServletRequest request, HttpServletResponse response,
									 LoginRequestDTO loginRequestDTO) {

		// 1. Recupero configurazione in base al method scelto
		ExternalAuthNoSecretDTO externalAuthNoSecret = externalAuthService.getExternalAuthNoSecret(loginRequestDTO.getAuthMethod());

		// 2. Preparazione Stato e Redirect
		String encryptedState = buildEncryptedState(loginRequestDTO.getAuthMethod());
		externalAuthNoSecret.setState(encryptedState + ';' + request.getHeader("Origin"));

		// 3. Chiamata al Server SSO
		log.info("LOGIN: Invio a SSO");

		Map<String, Object> ssoBody = callSso("/provider_login", HttpMethod.POST, externalAuthNoSecret);

		log.info("LOGIN: SSO Success");
		return buildLoginResult(ssoBody);
	}

	@Override
	@Transactional(readOnly = true)
	public void callback(String code, String state, HttpServletResponse response) {

		// Decifrare lo state per estrarre authMethod
		String authMethod = decryptStateAndExtractAuthMethod(state.split(";")[0]);
		ExternalAuthWithSecretDTO authConfig = externalAuthService.getExternalAuthWithSecret(authMethod);

		String scope = cfSystemConfigRepository.findByCfgKey("scopeSso")
				.map(CfSystemConfig::getCfgValue)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Scope non trovato a DB"));

		// Costruzione payload JSON
		ObjectNode payload = new ObjectMapper().convertValue(authConfig, ObjectNode.class);
		payload.put("code", code);
		payload.put("state", state);
		payload.put("scope",scope);

		log.info("CALLBACK: Chiamata al server SSO...");
		Map<String, Object> body = callSso("/callback", HttpMethod.POST, payload.toString());
		log.info("CALLBACK: Chiamata al server SSO SUCCESS");

		log.info(body.toString());
		
		if (!body.containsKey("accessToken")) {
			log.info("CALLBACK: Risposta SSO vuota o senza access_token");
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Access token mancante nella risposta del server SSO");
		}

		try {
			String codeCrypt = Utils.encryptAES(
					 body.get("accessToken") + "refreshtoken=" +body.get("refreshToken"));
			log.info("CALLBACK: JWT ricevuto e cifrato correttamente");
			response.sendRedirect(buildRedirectUrl(codeCrypt, state, authConfig));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@Transactional
	public UserInfoDTO userInfo(HttpServletRequest request, String code) throws Exception {
		UserInfoDTO tokenDTO = buildUserInfoDTO(Utils.decryptAES(code));
		String email = tokenDTO.getEmail();
		boolean isInternal = email.contains("proconsul-group.com");

		CfUser user;

		if (isInternal) {
			log.info("USERINFO: Utente interno ({}), skip censimento e sync", email);
			user = userRepository.findByUsername(email)
					.orElseThrow(() -> {
						log.info("USERINFO: Utente interno non trovato: {}", email);
						return new ResponseStatusException(HttpStatus.NOT_FOUND,
								"L'utente " + email + " non è abilitato all'accesso");
					});

			if (Boolean.FALSE.equals(user.getIsActive())) {
				log.info("USERINFO: Utente disattivato, accesso negato: {}", email);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN,
						"L'utente " + email + " non ha i requisiti per l'accesso");
			}

		} else {
			Optional<CfUser> findUser = userRepository.findByUsername(email);

			if (findUser.isEmpty()) {
				log.info("USERINFO: Primo accesso, censimento utente da Graph: {}", email);
				user = createUser(tokenDTO);
			} else {
				user = findUser.get();
				if (user.getIsMarked() != null) {
					log.info("USERINFO: Utente marcato, accesso negato: {}", email);
					throw new ResponseStatusException(HttpStatus.FORBIDDEN,
							"L'utente " + email + " non ha i requisiti per l'accesso");
				}
				log.info("USERINFO: Utente trovato, avvio sync da Graph: {}", email);
				syncUserFromGraph(user, tokenDTO);
			}
		}

		return buildUserInfo(tokenDTO,user);
	}

//	@Override
//	public void logout(Integer idUser, Integ	er idRole, HttpServletRequest request, String refreshToken) {
//		if (refreshToken == null || refreshToken.isEmpty()) {
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh Token necessario per il logout");
//		}
//
//		String dominio = extractDomainFromAuthHeader(request);
//		ExternalAuth externalAuth = findExternalAuthByDomain(dominio);
//
//		LogoutRequestDTO logoutDto = externalAuthMapper.mapLogout(externalAuth);
//		logoutDto.setToken(refreshToken);
//		logoutDto.setTokenTypeHint("refreshToken");
//
//		String email = JWT.decode(extractAccessTokenFromRequest(request)).getClaim("email").asString();
//		log.info("LOGOUT: Invio revoca sessione (Refresh Token) a SSO per utente {}", email);
//
//		try {
//			restTemplate.postForEntity(ssoUrl + "/logout",
//					new HttpEntity<>(logoutDto, buildJsonHeaders()),
//					Void.class);
//			log.info("LOGOUT: Revoca sessione completata con successo per utente {}", email);
//		} catch (Exception e) {
//			log.info("LOGOUT: Fallita revoca su SSO per utente {}", email, e);
//			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante il logout");
//		}
//	}

	/**
	 * TO DO sistemare metodo per ottenre il refresh token, togliendo il dominio e utilizzando auth method
	 */
	/*@Override
	public Map<String, Object> getRefreshToken(RefreshDTO refreshDTO, HttpServletRequest request) {
		String dominio = null;
		ExternalAuth externalAuth = null;

		RefreshRequestDTO refreshRequestDTO = externalAuthMapper.mapRefresh(externalAuth);

		Map<String, String> ssoRequest = buildRefreshSsoRequest(refreshDTO.getRefreshToken(), refreshRequestDTO);

		log.info("REFRESH: Invio richiesta refresh token a SSO per dominio {}", dominio);
		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
					ssoUrl + "/refresh",
					HttpMethod.POST,
					new HttpEntity<>(ssoRequest),
					new ParameterizedTypeReference<Map<String, Object>>() {});

			log.info("REFRESH: Nuovo access_token ottenuto con successo per dominio {}", dominio);
			Map<String, Object> responseToken = new HashMap<>();
			responseToken.put("access_token", response.getBody().get("access_token"));
			return responseToken;
		} catch (Exception e) {
			log.info("REFRESH: Fallito refresh token per dominio {}", dominio, e);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh fallito");
		}
	}*/

	@Override
	public Map<String, Object> getRefreshToken(RefreshDTO refreshDTO) {

	    // Recupera ExternalAuth tramite authMethod
	    ExternalAuth externalAuthEntity = externalAuthRepository.findByAuthMethod(refreshDTO.getAuthMethod())
	            .orElseThrow(() -> {
	                log.info("REFRESH: authMethod non trovato: {}", refreshDTO.getAuthMethod());
	                return new ResponseStatusException(HttpStatus.NOT_FOUND,
	                        "Configurazione non trovata per authMethod: " + refreshDTO.getAuthMethod());
	            });

	    RefreshRequestDTO ssoRequest = externalAuthMapper.mapRefresh(externalAuthEntity,refreshDTO.getRefreshToken());
	    log.info("REFRESH: Invio richiesta refresh token a SSO per authMethod {}", refreshDTO.getAuthMethod());
	    try {
	        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
	                ssoUrl + "/refresh",
	                HttpMethod.POST,
	                new HttpEntity<>(ssoRequest),
                    new ParameterizedTypeReference<>() {
                    });

	        log.info("REFRESH: Nuovo access_token ottenuto con successo per authMethod {}", refreshDTO.getAuthMethod());
	        Map<String, Object> responseToken = new HashMap<>();
	        responseToken.put("accessToken", response.getBody().get("accessToken"));
	        return responseToken;

	    } catch (Exception e) {
	        log.info("REFRESH: Fallito refresh token per authMethod {}", refreshDTO.getAuthMethod(), e);
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh fallito");
	    }
	}

	/**
	 * Recupera un access token valido per Microsoft Graph.
	 */
	public String getGraphAccessToken(GraphTokenRequestDTO graphTokenRequestDTO) {

		ExternalAuth externalAuth = externalAuthRepository.findByAuthMethod(graphTokenRequestDTO.getAuthMethod())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Configurazione non trovata per authMethod: " + graphTokenRequestDTO.getAuthMethod()));

		OAuth2RequestDTO oAuth2RequestDTO = externalAuthMapper.mapToOAuth2Request(externalAuth);

		Map<String, Object> ssoBody = callSso("/graph-token", HttpMethod.POST, oAuth2RequestDTO);

		String graphToken = (String) ssoBody.get("accessToken");
		if (graphToken == null) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Graph access token non ricevuto da SSO");
		}
		return graphToken;
	}

	private UserInfoDTO buildUserInfo(UserInfoDTO tokenDTO, CfUser user) {

		CfRole role = cfRoleRepository.findById(user.getIdRoleFk())
				.orElseThrow(() -> new EntityNotFoundException(
						"Ruolo non trovato con id: " + user.getIdRoleFk()));

		log.info("USERINFO: Accesso autorizzato per {} con ruolo {}", tokenDTO.getEmail(), role.getName());
		tokenDTO.setIdUser(user.getId());
		tokenDTO.setIdRole(user.getIdRoleFk());
		tokenDTO.setRoleName(role.getName());
		tokenDTO.setRoleCode(user.getRole().getCode());
		tokenDTO.setDescrizioneRuolo(user.getRole().getDescription());
		if (user.getOrganizationalStructure() != null) {
			tokenDTO.setIdOrganizationalStructure_Fk(user.getOrganizationalStructure().getId());
			tokenDTO.setOrganizationalStructureName(user.getOrganizationalStructure().getName());
		} else {
			tokenDTO.setIdOrganizationalStructure_Fk(null);
			tokenDTO.setOrganizationalStructureName(null);
		}
		if (user.getOffice() != null) {
			tokenDTO.setIdOffice_Fk(user.getOffice().getId());
			tokenDTO.setOfficeName(user.getOffice().getName());
		} else {
			tokenDTO.setIdOffice_Fk(null);
			tokenDTO.setOfficeName(null);
		}
		tokenDTO.setIdProfile(user.getIdProfileFk());
		tokenDTO.setProfileCode(user.getProfile().getCode());
		return tokenDTO;
	}
	
	/**
	 * Costruisce gli {@link HttpHeaders} con Content-Type application/json.
	 */
	private HttpHeaders buildJsonHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	/**
	 * Cifra in AES origin (normalizzando "localhost") e
	 * restituisce lo state da includere nel flusso OAuth.
	 */
	private String buildEncryptedState(String origin) {
		if (origin != null && origin.contains("localhost")) {
			origin = "localhost";
		}
		try {
			return Utils.encryptAES(origin);
		} catch (Exception e) {
			log.info("LOGIN: Fallita cifratura AES per origin {}", origin, e);
			throw new RuntimeException("Errore durante la cifratura dello stato", e);
		}
	}

	/**
	 * Decifra lo state ricevuto nella callback OAuth e ne estrae il dominio.
	 */
	private String decryptStateAndExtractAuthMethod(String state) {
		try {
			String decryptedState = Utils.decryptAES(state);
			log.info("CALLBACK: AuthMethod estratto dallo state: {}", decryptedState);
			return decryptedState;
		} catch (Exception e) {
			log.info("CALLBACK: Errore decifratura state", e);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "State non valido", e);
		}
	}

	private String buildRedirectUrl(String jwt, String state, ExternalAuthWithSecretDTO config) throws Exception {
		String baseUrl = state.contains("localhost") ? "http://localhost:4200/callback" : config.getClientRedirectUrl();
		Utils.encryptAES(jwt);
		return baseUrl + "?state=" + jwt;
	}

	/**
	 * Esegue una chiamata REST verso il server SSO.
	 * Gestisce le eccezioni HTTP e di rete in modo uniforme.
	 *
	 * @param path   path relativo da appendere a {@code ssoUrl} (es. "/provider_login")
	 * @param method metodo HTTP da usare
	 * @param body   corpo della request (serializzato come JSON)
	 * @return il body della response come {@code Map<String, Object>}
	 */
	private Map<String, Object> callSso(String path, HttpMethod method, Object body) {
		String url = ssoUrl + path;
		HttpEntity<?> requestEntity = new HttpEntity<>(body, buildJsonHeaders());
		log.info("SSO: Chiamata {} {}", method, url);
		try {
			ResponseEntity<Map<String, Object>> ssoResponse = restTemplate.exchange(
					url, method, requestEntity,
                    new ParameterizedTypeReference<>() {
                    });

			Map<String, Object> responseBody = ssoResponse.getBody();
			if (responseBody == null) {
				log.info("SSO: Risposta vuota da {}", url);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Risposta SSO vuota");
			}
			log.info("SSO: Risposta ricevuta correttamente da {}", url);
			return responseBody;

		} catch (HttpStatusCodeException e) {
			log.info("SSO: Errore HTTP da {}. Status: {} - Body: {} - Headers: {}",
					url, e.getStatusCode(), e.getResponseBodyAsString(), e.getResponseHeaders(), e);
			throw new ResponseStatusException(e.getStatusCode(), "Errore SSO: " + e.getResponseBodyAsString(), e);

		} catch (RestClientException e) {
			log.info("SSO: Errore di rete/connessione verso {}: ", url, e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "SSO non raggiungibile", e);

		} catch (ResponseStatusException e) {
			throw e;

		} catch (Exception e) {
			log.info("SSO: Errore generico imprevisto verso {}: ", url, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno server", e);
		}
	}

	/**
	 * Costruisce il risultato finale del login a partire dal body restituito da SSO.
	 * Gestisce sia il flusso diretto (access_token) che il redirect OAuth (authorizationUrl).
	 */
	private Map<String, Object> buildLoginResult(Map<String, Object> ssoBody) {
		Map<String, Object> result = new HashMap<>();

		if (ssoBody.containsKey("accessToken")) {
			result.put("accessToken", ssoBody.get("accessToken"));
			result.put("refreshToken", ssoBody.get("refreshToken"));
			log.info("LOGIN: Flusso diretto — access_token presente nella risposta SSO");
		} else if (ssoBody.containsKey("authorizationUrl")) {
			result.put("authorizationUrl", ssoBody.get("authorizationUrl"));
			log.debug("LOGIN: Flusso OAuth redirect — authorizationUrl presente nella risposta SSO");
		}

		return result;
	}

	/**
	 * Decodifica il codice cifrato ricevuto nella userInfo, estrae JWT,refresh token e authMethod,
	 * e popola un {@link UserInfoDTO} con i claim presenti nel JWT.
	 */
	private UserInfoDTO buildUserInfoDTO(String code) {
		String accessToken;
		UserInfoDTO tokenDTO = new UserInfoDTO();
		if (code.contains("refreshtoken=")) {
			String separator = "refreshtoken=";
			int index = code.indexOf(separator);
			accessToken = code.substring(0, index);
			String refreshToken = code.substring(index + separator.length());
			tokenDTO.setRefreshToken(refreshToken);
			log.debug("USERINFO: Refresh token estratto dal codice cifrato");
		} else {
			accessToken = code;
		}

		DecodedJWT decodedJWT = JWT.decode(accessToken);
		String email = decodedJWT.getClaim("preferred_username").asString();
		String name = decodedJWT.getClaim("name").asString();

		String tid = decodedJWT.getClaim("tid").asString();
		String authMethod = externalAuthRepository.findByTenantId(tid)
				.map(ExternalAuth::getAuthMethod)
				.orElseThrow(() -> {
					log.error("USERINFO: Nessun authMethod trovato per tenantId {}", tid);
					return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
							"Tenant non configurato: " + tid);
				});

		if (email == null) {
			log.error("USERINFO: Claim 'email' mancante nel JWT");
			throw new IllegalStateException("Claim email mancante nel JWT");
		}

		tokenDTO.setEmail(email);
		tokenDTO.setName(name);
		tokenDTO.setAccessToken(accessToken);
		tokenDTO.setAuthMethod(authMethod);
		return tokenDTO;
	}

	@Override
	public void logout(Integer idUser, Integer idRole, HttpServletRequest request, String refreshToken) {
	    if (refreshToken == null || refreshToken.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh Token necessario per il logout");
	    }

	    String authMethod = extractAuthMethodFromRequest(request);

	    ExternalAuth externalAuth = externalAuthRepository.findByAuthMethod(authMethod)
	            .orElseThrow(() -> {
	                log.info("LOGOUT: authMethod non trovato: {}", authMethod);
	                return new ResponseStatusException(HttpStatus.NOT_FOUND,
	                        "Configurazione non trovata per authMethod: " + authMethod);
	            });

	    LogoutRequestDTO logoutDto = externalAuthMapper.mapLogout(externalAuth);
	    logoutDto.setToken(refreshToken);
	    logoutDto.setTokenTypeHint("refreshToken");

	    String email = JWT.decode(extractAccessTokenFromRequest(request)).getClaim("email").asString();
	    log.info("LOGOUT: Invio revoca sessione (Refresh Token) a SSO per utente {}", email);

	    try {
	        restTemplate.postForEntity(
	                ssoUrl + "/logout",
	                new HttpEntity<>(logoutDto, buildJsonHeaders()),
	                Void.class);
	        log.info("LOGOUT: Revoca sessione completata con successo per utente {}", email);
	    } catch (Exception e) {
	        log.info("LOGOUT: Fallita revoca su SSO per utente {}", email, e);
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante il logout");
	    }
	}
	
	
	/**
	 * Estrae il Bearer token grezzo dall'header Authorization.
	 */
	private String extractAccessTokenFromRequest(HttpServletRequest request) {
	    String authHeader = request.getHeader("Authorization");
	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
	                "Authorization header mancante o non valido");
	    }
	    return authHeader.substring(7);
	}
	/**
	 * Decodifica il JWT dall'Authorization header ed estrae il claim authMethod
	 * (mappato sul campo "azp" o su un claim custom, a seconda di come SSO emette il token).
	 * Usato da refresh e logout per identificare la configurazione ExternalAuth nel DB.
	 * NOTA: decodifica senza verifica della firma — la validazione è già garantita
	 * da Spring Security prima che la request arrivi al controller.
	 */
	private String extractAuthMethodFromRequest(HttpServletRequest request) {
	    String token = extractAccessTokenFromRequest(request);
	    DecodedJWT decoded = JWT.decode(token);

	    // Il claim che identifica il provider è "authMethod" se SSO lo inserisce custom,
	    // altrimenti si usa "azp" (authorized party) che Azure mette sempre nel JWT.
	    // Da verificare con i test reali cosa restituisce Microsoft.
	    String authMethod = decoded.getClaim("authMethod").asString();
	    if (authMethod == null) {
	        authMethod = decoded.getClaim("azp").asString();
	    }
	    if (authMethod == null) {
	        log.error("REFRESH/LOGOUT: Claim authMethod/azp non trovato nel JWT");
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
	                "Impossibile determinare authMethod dal token");
	    }
	    log.info("REFRESH/LOGOUT: authMethod estratto dal JWT: {}", authMethod);
	    return authMethod;
	}

	/**
	 * Crea un nuovo CfUser a partire dai dati Microsoft Graph.
	 */
	/**
	 * Crea un nuovo CfUser a partire dai dati Microsoft Graph.
	 */
	private CfUser createUser(UserInfoDTO tokenDTO) {
		GraphUserInfoDTO graphUser = fetchGraphUserInfo(tokenDTO);
		CfUser newUser = new CfUser();
		newUser.setUsername(tokenDTO.getEmail());
		newUser.setEmail(tokenDTO.getEmail());
		newUser.setFirstName(graphUser.getGivenName());
		newUser.setLastName(graphUser.getSurname());
		newUser.setExternalIdentity(graphUser.getId());
		newUser.setIsActive(true);
		newUser.setCreationDate(LocalDateTime.now());
		newUser.setIdRoleFk(resolveRoleId(graphUser));
		newUser.setIdOrganizationalStructureFk(resolveOrganizationalStructureId(graphUser));
		newUser.setIdOfficeFk(resolveOfficeId(graphUser));
		CfUser saved = userRepository.save(newUser);
		log.info("CREATE: Nuovo utente censito con IdUser={} per {}", saved.getId(), tokenDTO.getEmail());
		return saved;
	}

	/**
	 * Confronta i dati Graph con quelli a DB e aggiorna solo i campi divergenti.
	 * I campi controllati sono: ruolo, struttura organizzativa, ufficio.
	 */
	private void syncUserFromGraph(CfUser user, UserInfoDTO tokenDTO) {
		GraphUserInfoDTO graphUser = fetchGraphUserInfo(tokenDTO);
		boolean modifica = false;

		Long graphRoleId = resolveRoleId(graphUser);
		if (graphRoleId != null && !graphRoleId.equals(user.getIdRoleFk())) {
			log.info("SYNC: Ruolo cambiato per {}: DB={} → Graph={}",
					user.getUsername(), user.getIdRoleFk(), graphRoleId);
			user.setIdRoleFk(graphRoleId);
			modifica = true;
		}

		if (modifica) {
			userRepository.save(user);
			log.info("SYNC: Aggiornamento completato per {}", user.getUsername());
		} else {
			log.info("SYNC: Nessuna modifica rilevata per {}", user.getUsername());
		}
	}

	/**
	 * Richiama Microsoft Graph /users/{email} per ottenere i dati aggiornati dell'utente.
	 */
	private GraphUserInfoDTO fetchGraphUserInfo(UserInfoDTO tokenDTO) {

		GraphTokenRequestDTO graphTokenRequest = new GraphTokenRequestDTO();
		graphTokenRequest.setAuthMethod(tokenDTO.getAuthMethod());
		String graphToken = getGraphAccessToken(graphTokenRequest);

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(graphToken);
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		Map<String, CfSystemConfig> configMap = cfSystemConfigRepository.getConfigurations()
				.stream()
				.collect(Collectors.toMap(CfSystemConfig::getCfgKey, Function.identity()));

		CfSystemConfig roleConfig      = configMap.get("role");
		CfSystemConfig officeConfig    = configMap.get("office");
		CfSystemConfig structureConfig = configMap.get("OrganizationalStructure");

		String selectFields = Stream.of(
						roleConfig      != null ? roleConfig.getCfgValue()      : null,
						structureConfig != null ? structureConfig.getCfgValue() : null,
						officeConfig    != null ? officeConfig.getCfgValue()     : null
				)
				.filter(Objects::nonNull)
				.collect(Collectors.joining(","));

		try {
			ResponseEntity<GraphUserInfoDTO> response = restTemplate.exchange(
					"https://graph.microsoft.com/v1.0/users/" + tokenDTO.getEmail()
							+ "?$select=id,displayName,givenName,surname,mail," + selectFields + ",companyName",
					HttpMethod.GET, entity, GraphUserInfoDTO.class);


			GraphUserInfoDTO graphUser = response.getBody();
			validateField(graphUser.getDepartment(),   structureConfig, "OrganizationalStructure");
			validateField(graphUser.getJobTitle(),     roleConfig,      "role");
			validateField(graphUser.getOfficeLocation(), officeConfig,  "office");

			log.info("GRAPH RAW: id={}, givenName={}, surname={}, jobTitle={}, department={}, officeLocation={}",
					graphUser.getId(),
					graphUser.getGivenName(),
					graphUser.getSurname(),
					graphUser.getJobTitle(),
					graphUser.getDepartment(),
					graphUser.getOfficeLocation());

			return graphUser;

		} catch (HttpStatusCodeException e) {
			log.error("GRAPH: Errore chiamata Graph per {}: {} - {}",
					tokenDTO.getEmail(), e.getStatusCode(), e.getResponseBodyAsString());
			throw new ResponseStatusException(e.getStatusCode(),
					"Errore Graph: " + e.getResponseBodyAsString());
		}
	}

	/**
	 * Determina l'ID ruolo (jobTitle) a partire dai dati Graph.
	 */
	private Long resolveRoleId(GraphUserInfoDTO graphUser) {
		if (graphUser.getJobTitle() == null) return null;
		return cfRoleRepository.findByNameIgnoreCase(graphUser.getJobTitle())
				.map(CfRole::getId)
				.orElse(null);
	}

	private Long resolveOrganizationalStructureId(GraphUserInfoDTO graphUser) {
		if (graphUser.getDepartment() == null) return null;
		return ceOrganizationalStructureRepository.findByNameIgnoreCase(graphUser.getDepartment())
				.map(CeOrganizationalStructure::getId)
				.orElse(null);
	}

	private Long resolveOfficeId(GraphUserInfoDTO graphUser) {
		if (graphUser.getOfficeLocation() == null) return null;
		return ceOfficeRepository.findByNameIgnoreCase(graphUser.getOfficeLocation())
				.map(CeOffice::getId)
				.orElse(null);
	}

	private void validateField(String value, CfSystemConfig config, String keyName) {
		if (value == null && config != null && Boolean.TRUE.equals(config.getIsRequired())) {
			throw new RuntimeException("Errore per il valore associato alla chiave: '" + keyName + "'");
		}
	}
}
