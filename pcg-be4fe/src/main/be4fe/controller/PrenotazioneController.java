package be4fe.controller;

import be4fe.dto.DisponibilitaPrenotazioneDTO;
import be4fe.dto.PrenotazioneDTO;
import be4fe.dto.RichiestaPrenotazioneDTO;
import be4fe.dto.RiepilogoStanzaDTO;
import be4fe.service.PrenotazioneService;
import common.base.BaseController;
import common.dto.CustomUserPrincipalDTO;
import common.model.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/prenotazioni")
@Tag(name = "Prenotazioni", description = "Prenotazione postazioni e sale")
public class PrenotazioneController extends BaseController<PrenotazioneService, PrenotazioneDTO> {

    public PrenotazioneController(PrenotazioneService prenotazioneService) {
        super(prenotazioneService);
    }

    @GetMapping("/disponibili")
    public List<DisponibilitaPrenotazioneDTO> trovaDisponibili(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.trovaDisponibili(utenteCorrente, data);
    }

    @GetMapping("/stanze/riepilogo/{id}")
    public RiepilogoStanzaDTO trovaRiepilogoStanza(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @PathVariable Long id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.riepilogoStanza(utenteCorrente, id, data);
    }

    @GetMapping("/prenotate")
    public List<PrenotazioneDTO> trovaPrenotate(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.trovaPrenotate(utenteCorrente, data);
    }

    @GetMapping("/mie")
    public List<PrenotazioneDTO> trovaMiePrenotazioni(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.trovaMiePrenotazioni(utenteCorrente, data);
    }

    @PostMapping
    public ResponseEntity<MessageResponse> creaPrenotazione(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @RequestBody RichiestaPrenotazioneDTO richiesta) {
        MessageResponse message = service.creaPrenotazione(utenteCorrente, richiesta);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{idPrenotazione}")
    public ResponseEntity<Void> eliminaPrenotazione(@AuthenticationPrincipal CustomUserPrincipalDTO utenteCorrente, @PathVariable Long idPrenotazione) {
        service.eliminaPrenotazione(utenteCorrente, idPrenotazione);
        return ResponseEntity.noContent().build();
    }
}
