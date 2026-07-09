package be4fe.service;

import be4fe.dto.SideMenuDTO;
import common.base.BaseRestService;

import java.util.List;

public interface SideMenuService extends BaseRestService<SideMenuDTO> {

	/**
	 * Recupera e costruisce la struttura gerarchica del menu laterale in base al ruolo dell'utente.
	 * <p>
	 * Il processo avviene in tre fasi:
	 * <ol>
	 * <li>Recupero della lista piatta delle funzioni associate al ruolo dal database.</li>
	 * <li>Mappatura di ogni entità in un oggetto {@link SideMenuDTO} e inserimento in una Map per accesso rapido.</li>
	 * <li>Costruzione dell'albero collegando ogni nodo al proprio genitore o aggiungendolo alla radice se il {@code parentId} è 0.</li>
	 * </ol>
	 * </p>
	 *
	 * @param idRole L'identificativo del ruolo dell'utente per cui filtrare le voci di menu.
	 * @return Una {@link List} di {@link SideMenuDTO} rappresentanti i nodi radice (root) del menu,
	 * ognuno dei quali contiene ricorsivamente i propri sotto-elementi (items).
	 */
	List<SideMenuDTO> getMenu(Long idRole);

}
