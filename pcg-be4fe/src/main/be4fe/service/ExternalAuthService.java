package be4fe.service;

import common.base.BaseDTO;
import common.base.BaseRestService;
import common.dto.ExternalAuthNoSecretDTO;
import common.dto.ExternalAuthWithSecretDTO;

public interface ExternalAuthService extends BaseRestService<BaseDTO> {

	/**
	 * Recupera la configurazione di autenticazione esterna senza dati sensibili.
	 *
	 * @param dominio dominio estratto dall'email utente
	 * @return configurazione senza secret
	 */
	ExternalAuthNoSecretDTO getExternalAuthNoSecret(String dominio);

	/**
	 * Recupera la configurazione completa di autenticazione esterna,
	 * inclusi i dati sensibili (client secret, ecc.).
	 *
	 * @param dominio dominio estratto dall'email utente
	 * @return configurazione completa con secret
	 */
	ExternalAuthWithSecretDTO getExternalAuthWithSecret(String dominio);

	/**
	 * Recupera la configurazione necessaria per il redirect del client
	 * durante il flusso di autenticazione (es. OAuth redirect).
	 *
	 * @param dominio dominio estratto dall'email utente
	 * @return configurazione con informazioni di redirect
	 */
	ExternalAuthWithSecretDTO getClientRedirectUrl(String dominio);

}
