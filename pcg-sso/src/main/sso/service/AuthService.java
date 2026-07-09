package sso.service;

import common.dto.OAuth2RequestDTO;
import sso.dto.AzureTokenResponseDTO;
import sso.dto.LogoutRequestDTO;
import common.dto.RefreshRequestDTO;

import java.util.Map;

public interface AuthService {

    /**
     * Genera l'URL di autorizzazione per il provider specificato.
     * @param oAuth2RequestDTO DTO contenente i parametri necessari (clientId, tenantId, state, authMethod).
     * @return Una Map contenente l'URL di redirect.
     * @throws org.springframework.web.server.ResponseStatusException se il provider non è supportato.
     */
    Map<String, Object> getProviderLoginUrl(OAuth2RequestDTO oAuth2RequestDTO);

    /**
     * Gestisce lo scambio del codice di autorizzazione con i token (Access, Refresh, ID Token).
     * @param oAuth2RequestDTO DTO contenente il 'code' restituito dal provider e le credenziali del client.
     * @return Un oggetto (solitamente un DTO o una Map) contenente i token ottenuti.
     * @throws org.springframework.web.server.ResponseStatusException in caso di errore nello scambio o provider non supportato.
     */
    Object callback(OAuth2RequestDTO oAuth2RequestDTO);

    /**
     * Esegue il rinnovo dei token utilizzando un Refresh Token valido.
     * @param refreshRequestDTO DTO contenente il refresh token, le credenziali del client e l'authMethod.
     * @return Un oggetto contenente il nuovo set di token.
     * @throws org.springframework.web.server.ResponseStatusException in caso di refresh fallito o provider non supportato.
     */
    Object refresh(RefreshRequestDTO refreshRequestDTO);

    /**
     * Gestisce la terminazione della sessione o la revoca dei token presso il provider.
     * @param logoutRequestDTO DTO contenente i dati della sessione da terminare e l'authMethod.
     * @return Una Map l'URL di logout per il redirect.
     * @throws org.springframework.web.server.ResponseStatusException se il logout fallisce o il provider non è supportato.
     */
    Map<String, Object> logout(LogoutRequestDTO logoutRequestDTO);

    /**
     * Ottiene un access token per Microsoft Graph
     */

    AzureTokenResponseDTO getGraphToken(OAuth2RequestDTO oAuth2RequestDTO);

}
