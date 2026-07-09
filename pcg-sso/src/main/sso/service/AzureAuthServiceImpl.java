package sso.service;

import common.dto.OAuth2RequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import sso.dto.AzureTokenResponseDTO;
import sso.dto.LogoutRequestDTO;
import common.dto.RefreshRequestDTO;
import sso.exception.EmptyAzureResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzureAuthServiceImpl implements AzureAuthService {

    private final RestTemplate restTemplate;
    private final static String baseAzureUrl = "https://login.microsoftonline.com/";

    @Override
    public Map<String, Object> login(OAuth2RequestDTO oAuth2RequestDTO) {

        log.info("LOGIN-AZURE: Inizio processo per tenantId: {}, clientId: {}",
                oAuth2RequestDTO.getTenantId(),
                oAuth2RequestDTO.getClientId());

        String authorizationUrl = buildAzureAuthorizationUrl(
                oAuth2RequestDTO.getTenantId(),
                oAuth2RequestDTO.getClientId(),
                oAuth2RequestDTO.getRedirectUri(),
                oAuth2RequestDTO.getState()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("authorizationUrl", authorizationUrl);

        log.info("LOGIN-AZURE: Authorization URL generata con successo");

        return response;
    }

    @Override
    public AzureTokenResponseDTO callback(OAuth2RequestDTO oAuth2RequestDTO) {

        String url = buildAzureTokenEndpoint(oAuth2RequestDTO.getTenantId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", oAuth2RequestDTO.getClientId());
        body.add("client_secret", oAuth2RequestDTO.getClientSecret());
        body.add("code", oAuth2RequestDTO.getCode());
        body.add("redirect_uri", oAuth2RequestDTO.getRedirectUri());
        body.add("scope", oAuth2RequestDTO.getScope());
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            log.info("AZURE-CALLBACK: Code exchange per tenant {}", oAuth2RequestDTO.getTenantId());

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getBody() == null) {
                throw new EmptyAzureResponseException();
            }

            AzureTokenResponseDTO tokenResponse = AzureTokenResponseDTO.builder()
                    .accessToken((String) response.getBody().get("access_token"))
                    .refreshToken((String) response.getBody().get("refresh_token"))
                    .idToken((String) response.getBody().get("id_token"))
                    .tokenType((String) response.getBody().get("token_type"))
                    .scope((String) response.getBody().get("scope"))
                    .expiresIn(body.get("expires_in") != null ? Long.valueOf(response.getBody().get("expires_in").toString()) : null)
                    .build();

            log.info("AZURE-CALLBACK: Code exchange completato con successo");

            return tokenResponse;

        } catch (HttpStatusCodeException e) {
            log.error("AZURE-CALLBACK: Errore Azure - Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new ResponseStatusException(
                    e.getStatusCode(),
                    "Errore Azure: " + e.getResponseBodyAsString()
            );

        } catch (Exception e) {
            log.error("AZURE-CALLBACK: Errore generico durante il callback", e);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore interno SSO"
            );
        }
    }

    @Override
    public AzureTokenResponseDTO refresh(RefreshRequestDTO refreshRequestDTO) {

        String url = buildAzureTokenEndpoint(refreshRequestDTO.getTenantId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", refreshRequestDTO.getClientId());
        body.add("refresh_token", refreshRequestDTO.getRefreshToken());
        body.add("client_secret", refreshRequestDTO.getClientSecret());
        body.add("scope", refreshRequestDTO.getClientId() + "/.default");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            log.info("AZURE-REFRESH: Refresh token per tenant {}", refreshRequestDTO.getTenantId());

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getBody() == null) {
                throw new EmptyAzureResponseException();
            }

            AzureTokenResponseDTO tokenResponse = AzureTokenResponseDTO.builder()
                    .accessToken((String) response.getBody().get("access_token"))
                    .build();

            log.info("AZURE-REFRESH: Refresh completato con successo");

            return tokenResponse;

        } catch (HttpStatusCodeException e) {
            log.error("AZURE-REFRESH: Errore Azure - Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new ResponseStatusException(
                    e.getStatusCode(),
                    "Errore Azure: " + e.getResponseBodyAsString()
            );

        } catch (Exception e) {
            log.error("AZURE-REFRESH: Errore generico durante il refresh token", e);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore interno SSO"
            );
        }
    }

    @Override
    public Map<String, Object> logout(LogoutRequestDTO logoutRequestDTO) {

        String logoutUrl = buildAzureLogoutUrl(
                logoutRequestDTO.getTenantId(),
                logoutRequestDTO.getPostLogoutRedirectUri(),
                logoutRequestDTO.getIdToken()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("logoutUrl", logoutUrl);
        log.info("AZURE-LOGOUT: Redirect verso Azure logout per tenant {}", logoutRequestDTO.getTenantId());

        return response;
    }

    /**
     * Ottiene un access token per Microsoft Graph
     */
    @Override
    public AzureTokenResponseDTO getGraphToken(OAuth2RequestDTO oAuth2RequestDTO) {

        String url = buildAzureTokenEndpoint(oAuth2RequestDTO.getTenantId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", oAuth2RequestDTO.getClientId());
        body.add("client_secret", oAuth2RequestDTO.getClientSecret());
        body.add("scope", "https://graph.microsoft.com/.default");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            log.info("AZURE-GRAPH-CC: Richiesta Graph token (client_credentials) per tenant {}",
                    oAuth2RequestDTO.getTenantId());

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getBody() == null) {
                throw new EmptyAzureResponseException();
            }

            AzureTokenResponseDTO tokenResponse = AzureTokenResponseDTO.builder()
                    .accessToken((String) response.getBody().get("access_token"))
                    .tokenType((String) response.getBody().get("token_type"))
                    .expiresIn(response.getBody().get("expires_in") != null
                            ? Long.valueOf(response.getBody().get("expires_in").toString())
                            : null)
                    .build();

            log.info("AZURE-GRAPH-CC: Graph token ottenuto con successo");
            return tokenResponse;

        } catch (HttpStatusCodeException e) {
            log.error("AZURE-GRAPH-CC: Errore Azure - Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(e.getStatusCode(),
                    "Errore Azure Graph: " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("AZURE-GRAPH-CC: Errore generico durante il Graph token", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore interno SSO Graph");
        }
    }

    private String buildAzureLogoutUrl(String tenantId,
                                       String postLogoutRedirectUri,
                                       String idToken) {

        StringBuilder url = new StringBuilder();

        url.append(baseAzureUrl)
                .append(URLEncoder.encode(tenantId, StandardCharsets.UTF_8))
                .append("/oauth2/v2.0/logout");

        boolean hasQueryParam = false;

        if (!isBlank(postLogoutRedirectUri)) {
            url.append("?post_logout_redirect_uri=")
                    .append(URLEncoder.encode(postLogoutRedirectUri, StandardCharsets.UTF_8));
            hasQueryParam = true;
        }

        if (!isBlank(idToken)) {
            url.append(hasQueryParam ? "&" : "?")
                    .append("id_token_hint=")
                    .append(URLEncoder.encode(idToken, StandardCharsets.UTF_8));
        }

        return url.toString();
    }

    private String buildAzureAuthorizationUrl(String tenantId,
                                              String clientId,
                                              String redirectUri,
                                              String state) {

        return baseAzureUrl
                + tenantId
                + "/oauth2/v2.0/authorize"
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&response_mode=query"
                + "&scope=" + URLEncoder.encode("openid profile email offline_access", StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
    }

    private String buildAzureTokenEndpoint(String tenantId) {
        return baseAzureUrl
                + tenantId
                + "/oauth2/v2.0/token";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}