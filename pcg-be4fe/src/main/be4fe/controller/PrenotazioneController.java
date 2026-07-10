package be4fe.controller;

import be4fe.dto.*;
import be4fe.service.PrenotazioneService;
import common.base.BaseController;
import common.dto.CustomUserPrincipalDTO;
import common.model.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/prenotazioni")
@Tag(name = "Prenotazioni", description = "Prenotazione postazioni e sale")
public class PrenotazioneController extends BaseController<PrenotazioneService, PrenotazioneDTO> {

    public PrenotazioneController(PrenotazioneService prenotazioneService) {
        super(prenotazioneService);
    }

    @Operation(
            summary = "Recupera le postazioni disponibili",
            description = "Restituisce le workspace prenotabili nella data indicata, filtrate in base al profilo dell'utente corrente e al blocco prenotazioni."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista delle postazioni disponibili restituita correttamente"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato")
    })
    @GetMapping("/disponibili")
    public List<DisponibilitaPrenotazioneDTO> trovaDisponibili(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.trovaDisponibili(utenteCorrente, data);
    }

    @Operation(
            summary = "Recupera il riepilogo di una stanza",
            description = "Restituisce posti totali, posti occupati e posti disponibili per la workspace indicata nella data richiesta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Riepilogo stanza restituito correttamente"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "404", description = "Stanza non trovata")
    })
    @GetMapping("/stanze/riepilogo/{id}")
    public RiepilogoStanzaDTO trovaRiepilogoStanza(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @PathVariable Long id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.riepilogoStanza(utenteCorrente, id, data);
    }

    @Operation(
            summary = "Recupera le prenotazioni visibili",
            description = "Restituisce le prenotazioni per data singola o intervallo. L'admin vede tutte le prenotazioni non marcate, gli altri profili vedono solo quelle delle workspace abilitate."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista prenotazioni restituita correttamente"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato")
    })
    @GetMapping("/prenotate")
    public List<PrenotazioneDTO> trovaPrenotate(
            @AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDa,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataA
    ) {
        return service.trovaPrenotate(utenteCorrente, data, dataDa, dataA);
    }

    @Operation(
            summary = "Recupera le mie prenotazioni",
            description = "Restituisce le prenotazioni dell'utente corrente. Se viene passata una data, filtra le prenotazioni su quel giorno."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista delle prenotazioni personali restituita correttamente"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato")
    })
    @GetMapping("/mie")
    public List<PrenotazioneDTO> trovaMiePrenotazioni(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.trovaMiePrenotazioni(utenteCorrente, data);
    }

    @Operation(
            summary = "Crea una prenotazione",
            description = "Crea una prenotazione per la workspace richiesta. Per le workspace non esclusive richiede anche il posto/sedia da prenotare."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prenotazione creata correttamente"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Profilo non abilitato alla prenotazione"),
            @ApiResponse(responseCode = "404", description = "Workspace o posto non trovato"),
            @ApiResponse(responseCode = "409", description = "Prenotazione non disponibile per data, posto, capienza o blocco")
    })
    @PostMapping
    public ResponseEntity<MessageResponse> creaPrenotazione(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @RequestBody RichiestaPrenotazioneDTO richiesta) {
        MessageResponse message = service.creaPrenotazione(utenteCorrente, richiesta);
        return ResponseEntity.ok(message);
    }

    @Operation(
            summary = "Elimina una prenotazione",
            description = "Cancella logicamente una prenotazione. L'utente normale puo eliminare solo le proprie, l'admin puo eliminare anche quelle di altri utenti."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Prenotazione eliminata correttamente"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "404", description = "Prenotazione non trovata")
    })
    @DeleteMapping("/{idPrenotazione}")
    public ResponseEntity<Void> eliminaPrenotazione(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @PathVariable Long idPrenotazione) {
        service.eliminaPrenotazione(utenteCorrente, idPrenotazione);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Crea un blocco prenotazioni",
            description = "Crea un blocco globale delle prenotazioni per un intervallo di date. Operazione riservata al profilo admin."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blocco prenotazioni creato correttamente"),
            @ApiResponse(responseCode = "400", description = "Date blocco non valide"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Profilo non abilitato alla gestione amministrativa")
    })
    @PostMapping("/admin/blocchi")
    public ResponseEntity<MessageResponse> creaBloccoPrenotazioni(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @RequestBody RichiestaBloccoPrenotazioniDTO richiesta) {
        MessageResponse message = service.creaBloccoPrenotazioni(utenteCorrente, richiesta);
        return ResponseEntity.ok(message);
    }

    @Operation(
            summary = "Recupera i blocchi prenotazioni",
            description = "Restituisce i blocchi prenotazioni attivi, filtrabili per data singola o intervallo. Operazione riservata al profilo admin."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista blocchi restituita correttamente"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Profilo non abilitato alla gestione amministrativa")
    })
    @GetMapping("/admin/blocchi")
    public List<BloccoPrenotazioniDTO> trovaBlocchiPrenotazioni(
            @AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDa,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataA
    ) {
        return service.trovaBlocchiPrenotazioni(utenteCorrente, data, dataDa, dataA);
    }

    @Operation(
            summary = "Elimina un blocco prenotazioni",
            description = "Cancella logicamente un blocco prenotazioni esistente. Operazione riservata al profilo admin."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blocco prenotazioni eliminato correttamente"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Profilo non abilitato alla gestione amministrativa"),
            @ApiResponse(responseCode = "404", description = "Blocco prenotazioni non trovato")
    })
    @DeleteMapping("/admin/blocchi/{idBlocco}")
    public ResponseEntity<MessageResponse> eliminaBloccoPrenotazioni(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @PathVariable Long idBlocco) {
        MessageResponse message = service.eliminaBloccoPrenotazioni(utenteCorrente, idBlocco);
        return ResponseEntity.ok(message);
    }

    @Operation(
            summary = "Recupera i posti di una workspace",
            description = "Restituisce i posti/sedie della workspace indicata e segnala quali risultano occupati nella data richiesta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista posti restituita correttamente"),
            @ApiResponse(responseCode = "400", description = "Workspace non compatibile con la prenotazione del singolo posto"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Profilo non abilitato alla consultazione"),
            @ApiResponse(responseCode = "404", description = "Workspace non trovata")
    })
    @GetMapping("/workspace/{idWorkspace}/posti")
    public List<PostoWorkspaceDTO> trovaPostiWorkspace(
            @AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente,
            @PathVariable Long idWorkspace,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        return service.trovaPostiWorkspace(utenteCorrente, idWorkspace, data);
    }

}
