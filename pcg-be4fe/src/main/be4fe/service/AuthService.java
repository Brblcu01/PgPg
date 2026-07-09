package be4fe.service;

import be4fe.dto.LoginRequestDTO;
import be4fe.dto.RefreshDTO;
import be4fe.dto.UserInfoDTO;
import common.base.BaseDTO;
import common.base.BaseRestService;
import common.dto.GraphTokenRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Service principale per la gestione del login SSO tramite Azure AD.
 * Gestisce:
 * - avvio login OAuth2
 * - callback Azure
 * - refresh token
 * - recupero user info
 * I token vengono sempre cifrati tramite AES prima di essere esposti al frontend.
 */
public interface AuthService extends BaseRestService<BaseDTO> {

    /**
     * Avvia il flusso di autenticazione verso il server SSO.
     * Valida l'email, recupera la configurazione del dominio, cifra lo state AES
     * e contatta l'SSO. Restituisce {@code isPasswordRequired}, {@code access_token}
     * o {@code authorizationUrl} a seconda del flusso.
     *
     * @param request         per recuperare l'header Origin
     * @param response        per impostare lo status HTTP in caso di errore
     * @param loginRequestDTO credenziali utente (username, password opzionale)
     * @return mappa con l'esito del login
     * @throws ResponseStatusException in caso di errori SSO o di rete
     */
    Map<String, Object> login(HttpServletRequest request, HttpServletResponse response,
                              LoginRequestDTO loginRequestDTO);

    /**
     * Gestisce la callback OAuth2: decifra lo state, scambia l'authorization code
     * con il JWT tramite SSO, cifra il token e reindirizza il client al portale.
     *
     * @param code     authorization code ricevuto dal provider
     * @param state    state cifrato AES contenente dominio e origin
     * @param response per eseguire il redirect finale
     * @throws ResponseStatusException se lo state è invalido o l'SSO risponde con errore
     */
    void callback(String code, String state, HttpServletResponse response);

    /**
     * Decripta il codice cifrato, verifica l'utente nel DB (esistenza e requisiti)
     * e restituisce le informazioni arricchite con ID utente e ruolo.
     *
     * @param request per contesto di audit/log
     * @param code    stringa cifrata contenente access token e refresh token
     * @return {@link UserInfoDTO} con email, nome, JWT, ID utente e ruolo
     * @throws ResponseStatusException se l'utente non è abilitato (404) o marcato (403)
     * @throws EntityNotFoundException se il ruolo associato non esiste
     * @throws Exception               in caso di errore nella decifratura AES
     */
    UserInfoDTO userInfo(HttpServletRequest request, String code) throws Exception;

    /**
     * Revoca il refresh token dell'utente sul server SSO.
     * Estrae il dominio dal JWT in Authorization header, recupera la configurazione
     * e invia la richiesta di revoca.
     *
     * @param idUser       ID utente (per log/audit)
     * @param idRole       ID ruolo (per log/audit)
     * @param request      da cui estrarre il Bearer token
     * @param refreshToken token da revocare
     * @throws ResponseStatusException {@code 400} se il refresh token è assente,
     *                                 {@code 401} se il Bearer è mancante,
     *                                 {@code 404} se il dominio non è configurato,
     *                                 {@code 500} se la revoca SSO fallisce
     */
    void logout(Integer idUser, Integer idRole, HttpServletRequest request, String refreshToken);

    /**
     * Rinnova l'access token contattando l'SSO con il refresh token fornito.
     * Il dominio viene estratto dal JWT nell'Authorization header.
     *
     * @param refreshDTO DTO contenente il refresh token
     * @return mappa con il nuovo {@code access_token}
     * @throws ResponseStatusException {@code 401} se il refresh fallisce
     */
    Map<String, Object> getRefreshToken(RefreshDTO refreshDTO);

    String getGraphAccessToken(GraphTokenRequestDTO graphTokenRequestDTO);
}
