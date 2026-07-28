package be4fe.service;

import be4fe.dto.*;
import be4fe.mapper.PrenotazioneConverter;
import common.base.BaseGenericRestService;
import common.dto.CustomUserPrincipalDTO;
import common.entity.CeBooking;
import common.entity.CeBookingBlock;
import common.entity.CeWorkspace;
import common.entity.CeWorkspaceSeat;
import common.model.MessageResponse;
import common.repository.*;
import common.utils.MessageResponseFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
public class PrenotazioneServiceImpl extends BaseGenericRestService<CeBooking, PrenotazioneDTO, CeBookingRepository> implements PrenotazioneService {

    private static final String PROFILO_ADMIN = "ADMIN";
    private static final String PROFILO_USER = "USER";
    private static final String STATO_CONFERMATA = "CONFIRMED";
    private static final String STATO_CANCELLATA = "CANCELLATA";

    private final CeWorkspaceRepository risorsaRepository;
    private final CeBookableResourceProfileRepository profiloRisorsaRepository;
    private final CeBookingBlockRepository bloccoPrenotazioniRepository;
    private final CfProfileRepository profileRepository;
    private final PrenotazioneConverter prenotazioneConverter;
    private final CeWorkspaceSeatRepository workspaceSeatRepository;
    private final EmailService emailService;

    public PrenotazioneServiceImpl(CeBookingRepository prenotazioneRepository, PrenotazioneConverter prenotazioneConverter, CeWorkspaceRepository risorsaRepository, CeBookableResourceProfileRepository profiloRisorsaRepository, CeBookingBlockRepository bloccoPrenotazioniRepository, CfProfileRepository profileRepository,CeWorkspaceSeatRepository workspaceSeatRepository,EmailService emailService) {
        super(prenotazioneRepository, prenotazioneConverter);
        this.risorsaRepository = risorsaRepository;
        this.profiloRisorsaRepository = profiloRisorsaRepository;
        this.bloccoPrenotazioniRepository = bloccoPrenotazioniRepository;
        this.profileRepository = profileRepository;
        this.prenotazioneConverter = prenotazioneConverter;
        this.workspaceSeatRepository=workspaceSeatRepository;
        this.emailService=emailService;
    }

    @Override
    public List<DisponibilitaPrenotazioneDTO> trovaWorkspaceDisponibili(CustomUserPrincipalDTO user, LocalDate data) {

        return risorsaRepository.trovaTuttePrenotabili().stream()
                .filter(risorsa -> utentePuoPrenotare(user, risorsa.getId()))
                .filter(risorsa -> !prenotazioniBloccate(data))
                .map(risorsa -> creaDisponibilitaDTO(risorsa, data))
                .filter(disponibilita -> disponibilita.getPostiDisponibili() > 0)
                .toList();
    }

    @Override
    public RiepilogoStanzaDTO riepilogoWorkspace(CustomUserPrincipalDTO user, Long id, LocalDate data) {

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
    public List<PrenotazioneDTO> trovaPrenotazioni(CustomUserPrincipalDTO user, LocalDate data, LocalDate dataDa, LocalDate dataA) {

        if(utenteAdmin(user)){

            return repository
                    .trovaPrenotazioniAdmin(data, dataDa, dataA, PROFILO_USER)
                    .stream()
                    .map(prenotazione -> prenotazioneConverter.toDTO(prenotazione, user))
                    .toList();
        }

            return repository
                    .trovaPrenotazioniConfermate(data, dataDa, dataA, STATO_CONFERMATA)
                    .stream()
                    .filter(prenotazione -> utentePuoPrenotare(user, prenotazione.getIdRisorsaPrenotabileFk()))
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
                .trovaAttivaNonMarcataPerId(richiesta.getIdWorkspace())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Risorsa prenotabile non trovata o non attiva"
                ));

        CeWorkspaceSeat posto = null;
        boolean salaRiunioni = salaRiunioni(risorsa);

        if (Boolean.TRUE.equals(risorsa.getPrenotazioneEsclusiva())) {
            if (richiesta.getIdWorkspaceSeat() != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Non puoi indicare un posto per una sala a prenotazione esclusiva"
                );
            }
        } else {
            if (richiesta.getIdWorkspaceSeat() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Posto obbligatorio per questa postazione"
                );
            }

            posto = workspaceSeatRepository
                    .trovaAttivoNonMarcatoPerIdEWorkspace(richiesta.getIdWorkspaceSeat(), risorsa.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Posto non trovato"));

            if (repository.existsPrenotazioneConfermataPosto(posto.getId(), richiesta.getDataPrenotazione(), STATO_CONFERMATA)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Posto gia prenotato per la data selezionata");
            }

            if (richiesta.getHourStart() != null || richiesta.getHourEnd() != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Gli orari possono essere indicati solo per la sala riunioni"
                );
            }
        }

        if (!utentePuoPrenotare(user, risorsa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Profilo non abilitato alla prenotazione della risorsa");
        }

        if (prenotazioniBloccate(richiesta.getDataPrenotazione())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prenotazioni bloccate per la data selezionata");
        }

        if (salaRiunioni) {
            validaOrariSalaRiunioni(richiesta);

            if (repository.existsSovrapposizioneSalaRiunioni(
                    risorsa.getId(),
                    richiesta.getDataPrenotazione(),
                    richiesta.getHourStart(),
                    richiesta.getHourEnd(),
                    STATO_CONFERMATA
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Sala riunioni gia prenotata per l'orario selezionato"
                );
            }
        } else {
            if (richiesta.getHourStart() != null || richiesta.getHourEnd() != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Gli orari possono essere indicati solo per la sala riunioni"
                );
            }

            if (utenteHaGiaUnaPrenotazione(user, richiesta.getDataPrenotazione())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Hai gia una prenotazione per la data selezionata");
            }

            long prenotazioniConfermate = repository.contaPrenotazioniConfermate(
                    risorsa.getId(),
                    richiesta.getDataPrenotazione(),
                    STATO_CONFERMATA
            );

            if (!risorsaHaPostiDisponibili(risorsa, prenotazioniConfermate)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Posti esauriti per la data selezionata");
            }
        }

        CeBooking prenotazione = CeBooking.builder()
                .idRisorsaPrenotabileFk(risorsa.getId())
                .idWorkspaceSeatFk(posto != null ? posto.getId() : null)
                .idUtenteFk(user.getId())
                .dataPrenotazione(richiesta.getDataPrenotazione())
                .hourStart(richiesta.getHourStart())
                .hourEnd(richiesta.getHourEnd())
                .stato(STATO_CONFERMATA)
                .dataCreazione(LocalDateTime.now())
                .marcata(false)
                .build();

        CeBooking salvata = repository.save(prenotazione);
        salvata.setPostazioneLavoro(risorsa);
        salvata.setPosto(posto);

        return MessageResponseFactory.created();
    }

    @Override
    public void eliminaPrenotazione(CustomUserPrincipalDTO user, Long idPrenotazione) {

        if(utenteAdmin(user)){
            CeBooking prenotazione = repository.findById(idPrenotazione)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prenotazione non trovata"));

            boolean prenotazioneDiUser = repository.existsByIdAndUserProfileCode(prenotazione.getId(), PROFILO_USER);

            annullaPrenotazione(prenotazione);

            if(prenotazioneDiUser) {
                emailService.inviaNotificaPrenotazioneAnnullata(prenotazione.getId());
            }

        } else {

            CeBooking prenotazione = repository.findByIdAndIdUtenteFk(idPrenotazione, user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prenotazione non trovata"));

            annullaPrenotazione(prenotazione);
        }
    }


    @Override
    public MessageResponse creaBloccoPrenotazioni(CustomUserPrincipalDTO user, RichiestaBloccoPrenotazioniDTO richiesta) {

        verificaUtenteAdmin(user);

        CeBookingBlock blocco = CeBookingBlock.builder()
                .dataInizio(richiesta.getDataInizio())
                .dataFine(richiesta.getDataFine())
                .motivo(richiesta.getMotivo())
                .idUtenteCreazioneFk(user.getId())
                .dataCreazione(LocalDateTime.now())
                .marcata(false)
                .build();

        bloccoPrenotazioniRepository.save(blocco);
        return MessageResponseFactory.created();
    }

    @Override
    public List<BloccoPrenotazioniDTO> trovaBlocchiPrenotazioni(CustomUserPrincipalDTO user, LocalDate data, LocalDate dataDa, LocalDate dataA) {

        verificaUtenteAdmin(user);

        return bloccoPrenotazioniRepository
                .trovaBlocchiAttivi(data, dataDa, dataA)
                .stream()
                .map(this::creaBloccoPrenotazioniDTO)
                .toList();
    }

    @Override
    public MessageResponse eliminaBloccoPrenotazioni(CustomUserPrincipalDTO user, Long idBlocco) {

        verificaUtenteAdmin(user);

        CeBookingBlock blocco = bloccoPrenotazioniRepository.findById(idBlocco)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blocco prenotazioni non trovato"));

        blocco.setMarcata(true);
        blocco.setDataUltimoAggiornamento(LocalDateTime.now());
        bloccoPrenotazioniRepository.save(blocco);
        return MessageResponseFactory.deleted();
    }

    @Override
    public List<PostoWorkspaceDTO> trovaPostiWorkspace(
            CustomUserPrincipalDTO user,
            Long idWorkspace,
            LocalDate data
    ) {
        CeWorkspace workspace = risorsaRepository.findById(idWorkspace)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace non trovato"));

        if (!utenteAdmin(user) && !utentePuoPrenotare(user, workspace.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Profilo non abilitato alla consultazione della postazione");
        }

        if (Boolean.TRUE.equals(workspace.getPrenotazioneEsclusiva())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La workspace selezionata non ha posti prenotabili singolarmente");
        }

        return workspaceSeatRepository
                .trovaPostiPerWorkspace(workspace.getId())
                .stream()
                .map(posto -> PostoWorkspaceDTO.builder()
                        .idWorkspaceSeat(posto.getId())
                        .idWorkspace(posto.getIdWorkspaceFk())
                        .codice(posto.getCodice())
                        .nome(posto.getNome())
                        .occupato(repository.existsPrenotazioneConfermataPosto(
                                posto.getId(),
                                data,
                                STATO_CONFERMATA
                        ))
                        .build())
                .toList();
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

    private boolean utenteAdmin(CustomUserPrincipalDTO user) {
        return user != null
                && user.getIdProfile() != null
                && profileRepository.existsByIdAndCodeIgnoreCase(user.getIdProfile(), PROFILO_ADMIN);
    }

    private void verificaUtenteAdmin(CustomUserPrincipalDTO user) {
        if (!utenteAdmin(user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Profilo non abilitato alla gestione amministrativa delle prenotazioni"
            );
        }
    }

    private boolean prenotazioniBloccate(LocalDate data) {
        return data != null
                && bloccoPrenotazioniRepository.existsBloccoAttivo(data);
    }

    private boolean salaRiunioni(CeWorkspace risorsa) {
        return Boolean.TRUE.equals(risorsa.getPrenotazioneEsclusiva())
                && (contieneTesto(risorsa.getCodice(), "RIUN")
                || contieneTesto(risorsa.getNome(), "RIUN")
                || contieneTesto(risorsa.getTipoRisorsa(), "RIUN")
                || contieneTesto(risorsa.getTipoRisorsa(), "MEETING"));
    }

    private boolean contieneTesto(String testo, String valoreCercato) {
        return testo != null
                && valoreCercato != null
                && testo.toUpperCase().contains(valoreCercato.toUpperCase());
    }

    private void validaOrariSalaRiunioni(RichiestaPrenotazioneDTO richiesta) {
        LocalTime hourStart = richiesta.getHourStart();
        LocalTime hourEnd = richiesta.getHourEnd();

        if (hourStart == null || hourEnd == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ora inizio e ora fine sono obbligatorie per la sala riunioni"
            );
        }

        if (!hourEnd.isAfter(hourStart)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ora fine deve essere successiva a ora inizio"
            );
        }
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

    private void annullaPrenotazione(CeBooking prenotazione) {
        prenotazione.setStato(STATO_CANCELLATA);
        prenotazione.setMarcata(true);
        prenotazione.setDataUltimoAggiornamento(LocalDateTime.now());
        repository.save(prenotazione);
    }


    private BloccoPrenotazioniDTO creaBloccoPrenotazioniDTO(CeBookingBlock blocco) {
        return BloccoPrenotazioniDTO.builder()
                .idBlocco(blocco.getId())
                .dataInizio(blocco.getDataInizio())
                .dataFine(blocco.getDataFine())
                .motivo(blocco.getMotivo())
                .idUtenteCreazione(blocco.getIdUtenteCreazioneFk())
                .dataCreazione(blocco.getDataCreazione())
                .build();
    }
}
