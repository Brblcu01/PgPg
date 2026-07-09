package sso.service;

import common.dto.OAuth2RequestDTO;
import sso.dto.AzureTokenResponseDTO;
import sso.dto.LogoutRequestDTO;
import common.dto.RefreshRequestDTO;

import java.util.Map;

public interface AzureAuthService {

    /**
     * Costruisce la URL di autorizzazione Azure da usare per avviare il login OIDC.
     *
     * @param oAuth2RequestDTO dati necessari per generare la authorization URL
     * @return mappa contenente la authorization URL
     * @throws Exception in caso di errore nella costruzione della URL
     */
    Map<String, Object> login(OAuth2RequestDTO oAuth2RequestDTO) throws Exception;

    /**
     * Esegue il code exchange verso Azure e restituisce i token ottenuti.
     *
     * @param oAuth2RequestDTO dati necessari per scambiare authorization code con i token
     * @return response contenente access token, refresh token, id token ed expires in
     */
    AzureTokenResponseDTO callback(OAuth2RequestDTO oAuth2RequestDTO);

    /**
     * Richiede ad Azure un nuovo access token usando il refresh token.
     *
     * @param refreshRequestDTO dati necessari per il refresh dei token
     * @return response contenente i nuovi token aggiornati
     */
    AzureTokenResponseDTO refresh(RefreshRequestDTO refreshRequestDTO);

    /**
     * Costruisce la logout URL Azure da usare per il logout browser-based.
     *
     * @param logoutRequestDTO dati necessari per generare la logout URL
     * @return mappa contenente la logout URL
     * @throws Exception in caso di errore nella costruzione della URL
     */
    Map<String, Object> logout(LogoutRequestDTO logoutRequestDTO) throws Exception;

    AzureTokenResponseDTO getGraphToken(OAuth2RequestDTO oAuth2RequestDTO);

}