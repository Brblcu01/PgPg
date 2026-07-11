package be4fe.service;

import be4fe.dto.*;
import common.base.BaseRestService;
import common.dto.CustomUserPrincipalDTO;
import common.model.MessageResponse;

import java.time.LocalDate;
import java.util.List;

public interface PrenotazioneService extends BaseRestService<PrenotazioneDTO> {

    /**
     * Restituisce le workspace disponibili per la data indicata.
     * Le disponibilita sono filtrate in base al profilo dell'utente corrente e ai blocchi prenotazione attivi.
     *
     * @param utenteCorrente utente autenticato che effettua la richiesta
     * @param data data per cui verificare la disponibilita
     * @return lista delle workspace disponibili
     */
    List<DisponibilitaPrenotazioneDTO> trovaWorkspaceDisponibili(CustomUserPrincipalDTO utenteCorrente, LocalDate data);

    /**
     * Restituisce il riepilogo dei posti di una workspace in una data specifica.
     * Il riepilogo contiene posti totali, posti occupati e posti disponibili.
     *
     * @param utenteCorrente utente autenticato che effettua la richiesta
     * @param id identificativo della workspace
     * @param data data per cui calcolare il riepilogo
     * @return riepilogo dei posti della workspace
     */
    RiepilogoStanzaDTO riepilogoWorkspace(CustomUserPrincipalDTO utenteCorrente, Long id, LocalDate data);

    /**
     * Restituisce le prenotazioni visibili all'utente corrente.
     * L'admin visualizza tutte le prenotazioni non marcate; gli altri profili visualizzano solo quelle delle workspace abilitate.
     * La ricerca puo essere filtrata per data singola oppure per intervallo.
     *
     * @param utenteCorrente utente autenticato che effettua la richiesta
     * @param data data singola da consultare
     * @param dataDa data iniziale dell'intervallo
     * @param dataA data finale dell'intervallo
     * @return lista delle prenotazioni visibili
     */
    List<PrenotazioneDTO> trovaPrenotazioni(CustomUserPrincipalDTO utenteCorrente, LocalDate data, LocalDate dataDa, LocalDate dataA);

    /**
     * Restituisce le prenotazioni dell'utente corrente.
     * Se la data e valorizzata, restituisce solo le prenotazioni di quel giorno.
     *
     * @param utenteCorrente utente autenticato che effettua la richiesta
     * @param data data opzionale per filtrare le prenotazioni personali
     * @return lista delle prenotazioni dell'utente
     */
    List<PrenotazioneDTO> trovaMiePrenotazioni(CustomUserPrincipalDTO utenteCorrente, LocalDate data);

    /**
     * Crea una prenotazione per la workspace richiesta.
     * Per le workspace a prenotazione non esclusiva deve essere indicato anche il posto/sedia da prenotare.
     *
     * @param utenteCorrente utente autenticato che crea la prenotazione
     * @param richiesta dati necessari alla creazione della prenotazione
     * @return messaggio di esito della creazione
     */
    MessageResponse creaPrenotazione(CustomUserPrincipalDTO utenteCorrente, RichiestaPrenotazioneDTO richiesta);

    /**
     * Cancella logicamente una prenotazione.
     * L'utente normale puo cancellare solo le proprie prenotazioni; l'admin puo cancellare anche prenotazioni di altri utenti.
     *
     * @param utenteCorrente utente autenticato che richiede la cancellazione
     * @param idPrenotazione identificativo della prenotazione da cancellare
     */
    void eliminaPrenotazione(CustomUserPrincipalDTO utenteCorrente, Long idPrenotazione);

    /**
     * Crea un blocco globale delle prenotazioni per un intervallo di date.
     * Il blocco impedisce nuove prenotazioni nelle date comprese tra data inizio e data fine.
     *
     * @param utenteCorrente utente admin che crea il blocco
     * @param richiesta dati del blocco prenotazioni
     * @return messaggio di esito della creazione
     */
    MessageResponse creaBloccoPrenotazioni(CustomUserPrincipalDTO utenteCorrente, RichiestaBloccoPrenotazioniDTO richiesta);

    /**
     * Restituisce i blocchi prenotazioni attivi.
     * La ricerca puo essere filtrata per data singola oppure per intervallo.
     *
     * @param user utente admin che effettua la richiesta
     * @param data data singola da consultare
     * @param dataDa data iniziale dell'intervallo
     * @param dataA data finale dell'intervallo
     * @return lista dei blocchi prenotazioni attivi
     */
    List<BloccoPrenotazioniDTO> trovaBlocchiPrenotazioni(CustomUserPrincipalDTO user, LocalDate data, LocalDate dataDa, LocalDate dataA);

    /**
     * Cancella logicamente un blocco prenotazioni.
     *
     * @param utenteCorrente utente admin che richiede la cancellazione
     * @param idBlocco identificativo del blocco prenotazioni da cancellare
     * @return messaggio di esito della cancellazione
     */
    MessageResponse eliminaBloccoPrenotazioni(CustomUserPrincipalDTO utenteCorrente, Long idBlocco);

    /**
     * Restituisce i posti/sedie associati a una workspace e indica quali sono occupati nella data richiesta.
     *
     * @param utenteCorrente utente autenticato che effettua la richiesta
     * @param idWorkspace identificativo della workspace
     * @param data data per cui verificare l'occupazione dei posti
     * @return lista dei posti della workspace con indicazione di occupazione
     */
    List<PostoWorkspaceDTO> trovaPostiWorkspace(CustomUserPrincipalDTO utenteCorrente, Long idWorkspace, LocalDate data);
}
