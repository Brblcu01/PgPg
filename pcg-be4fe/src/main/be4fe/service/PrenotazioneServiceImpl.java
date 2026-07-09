package be4fe.service;

import be4fe.dto.DisponibilitaPrenotazioneDTO;
import be4fe.dto.PrenotazioneDTO;
import be4fe.dto.RichiestaPrenotazioneDTO;
import be4fe.dto.RiepilogoStanzaDTO;
import be4fe.mapper.PrenotazioneConverter;
import common.base.BaseGenericRestService;
import common.dto.CustomUserPrincipalDTO;
import common.entity.CeBooking;
import common.entity.CeWorkspace;
import common.model.MessageResponse;
import common.repository.CeBookableResourceProfileRepository;
import common.repository.CeBookingRepository;
import common.repository.CeWorkspaceRepository;
import common.utils.MessageResponseFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service

public class PrenotazioneServiceImpl extends BaseGenericRestService<CeBooking, PrenotazioneDTO, CeBookingRepository> implements PrenotazioneService {

    private static final String STATO_CONFERMATA = "CONFIRMED";

    private final CeWorkspaceRepository risorsaRepository;
    private final CeBookableResourceProfileRepository profiloRisorsaRepository;
    private final PrenotazioneConverter prenotazioneConverter;

    public PrenotazioneServiceImpl(CeBookingRepository prenotazioneRepository, PrenotazioneConverter prenotazioneConverter, CeWorkspaceRepository risorsaRepository, CeBookableResourceProfileRepository profiloRisorsaRepository) {
        super(prenotazioneRepository, prenotazioneConverter);
        this.risorsaRepository = risorsaRepository;
        this.profiloRisorsaRepository = profiloRisorsaRepository;
        this.prenotazioneConverter = prenotazioneConverter;
    }

    @Override
    public List<DisponibilitaPrenotazioneDTO> trovaDisponibili(CustomUserPrincipalDTO user, LocalDate data) {

        return risorsaRepository.trovaTuttePrenotabili().stream()
                .filter(risorsa -> utentePuoPrenotare(user, risorsa.getId()))
                .map(risorsa -> creaDisponibilitaDTO(risorsa, data))
                .filter(disponibilita -> disponibilita.getPostiDisponibili() > 0)
                .toList();
    }

    @Override
    public RiepilogoStanzaDTO riepilogoStanza(CustomUserPrincipalDTO user, Long id, LocalDate data) {

        CeWorkspace risorsa = risorsaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stanza non trovata"));

        long postiOccupati = repository.contaPrenotazioniConfermate(risorsa.getId(), data, STATO_CONFERMATA);

        return RiepilogoStanzaDTO.builder()
                .idRisorsaPrenotabile(risorsa.getId())
                .codice(risorsa.getCodice())
                .nome(risorsa.getNome())
                .postiTotali(risorsa.getCapienza())
                .postiOccupati(postiOccupati)
                .postiDisponibili(calcolaPostiDisponibili(risorsa, postiOccupati))
                .build();
    }

    @Override
    public List<PrenotazioneDTO> trovaPrenotate(CustomUserPrincipalDTO user, LocalDate data) {

        return repository
                .trovaPrenotazioniConfermate(data, STATO_CONFERMATA)
                .stream()
                .map(prenotazione -> prenotazioneConverter.toDTO(prenotazione, user))
                .toList();
    }

    @Override
    public List<PrenotazioneDTO> trovaMiePrenotazioni(CustomUserPrincipalDTO user, LocalDate data) {

        return repository
                .trovaPrenotazioniUtente(user.getId(), data)
                .stream()
                .map(prenotazione -> prenotazioneConverter.toDTO(prenotazione, user))
                .toList();
    }

    @Override
    public MessageResponse creaPrenotazione(CustomUserPrincipalDTO user, RichiestaPrenotazioneDTO richiesta) {

        CeWorkspace risorsa = risorsaRepository
                .trovaAttivaNonMarcataPerId(richiesta.getIdRisorsaPrenotabile())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Risorsa prenotabile non trovata o non attiva"
                ));

        if (!utentePuoPrenotare(user, risorsa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Profilo non abilitato alla prenotazione della risorsa");
        }

        if (utenteHaGiaUnaPrenotazione(user, richiesta.getDataPrenotazione())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hai gia una prenotazione per la data selezionata");
        }

        long prenotazioniConfermate = repository.contaPrenotazioniConfermate(risorsa.getId(), richiesta.getDataPrenotazione(), STATO_CONFERMATA);

        if (!risorsaHaPostiDisponibili(risorsa, prenotazioniConfermate)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Posti esauriti per la data selezionata");
        }

        CeBooking prenotazione = CeBooking.builder()
                .idRisorsaPrenotabileFk(risorsa.getId())
                .idUtenteFk(user.getId())
                .dataPrenotazione(richiesta.getDataPrenotazione())
                .stato(STATO_CONFERMATA)
                .dataCreazione(LocalDateTime.now())
                .marcata(false)
                .build();

        CeBooking salvata = repository.save(prenotazione);
        salvata.setPostazioneLavoro(risorsa);

        return MessageResponseFactory.created();
    }

    @Override
    public void eliminaPrenotazione(CustomUserPrincipalDTO user, Long idPrenotazione) {

        CeBooking prenotazione = repository.findByIdAndIdUtenteFk(idPrenotazione, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prenotazione non trovata"));

        prenotazione.setStato("CANCELLATA");
        prenotazione.setMarcata(true);
        prenotazione.setDataUltimoAggiornamento(LocalDateTime.now());
        repository.save(prenotazione);
    }

    private DisponibilitaPrenotazioneDTO creaDisponibilitaDTO(CeWorkspace risorsa, LocalDate data) {

        long prenotazioniConfermate = repository.contaPrenotazioniConfermate(risorsa.getId(), data, STATO_CONFERMATA);

        return DisponibilitaPrenotazioneDTO.builder()
                .idRisorsaPrenotabile(risorsa.getId())
                .codice(risorsa.getCodice())
                .nome(risorsa.getNome())
                .tipoRisorsa(risorsa.getTipoRisorsa())
                .capienza(risorsa.getCapienza())
                .prenotazioneEsclusiva(risorsa.getPrenotazioneEsclusiva())
                .prenotazioniConfermate(prenotazioniConfermate)
                .postiDisponibili(calcolaPostiDisponibili(risorsa, prenotazioniConfermate))
                .build();
    }

    private boolean utentePuoPrenotare(CustomUserPrincipalDTO user, Long idRisorsa) {
        return user != null
                && user.getIdProfile() != null
                && profiloRisorsaRepository.existsByIdRisorsaPrenotabileFkAndIdProfiloFk(idRisorsa, user.getIdProfile());
    }

    private boolean utenteHaGiaUnaPrenotazione(CustomUserPrincipalDTO user, LocalDate data) {
        return repository.contaPrenotazioniUtenteConfermatePerData(user.getId(), data, STATO_CONFERMATA) > 0;
    }

    private boolean risorsaHaPostiDisponibili(CeWorkspace risorsa, long prenotazioniConfermate) {
        return calcolaPostiDisponibili(risorsa, prenotazioniConfermate) > 0;
    }

    private long calcolaPostiDisponibili(CeWorkspace risorsa, long prenotazioniConfermate) {
        long capienza = risorsa.getCapienza() != null ? risorsa.getCapienza() : 0L;

        if (Boolean.TRUE.equals(risorsa.getPrenotazioneEsclusiva())) {
            return prenotazioniConfermate == 0 ? capienza : 0;
        }

        return Math.max(0, capienza - prenotazioniConfermate);
    }
}
