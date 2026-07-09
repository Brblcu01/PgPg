package be4fe.service;

import be4fe.dto.DisponibilitaPrenotazioneDTO;
import be4fe.dto.PrenotazioneDTO;
import be4fe.dto.RichiestaPrenotazioneDTO;
import be4fe.dto.RiepilogoStanzaDTO;
import common.base.BaseRestService;
import common.dto.CustomUserPrincipalDTO;
import common.model.MessageResponse;

import java.time.LocalDate;
import java.util.List;

public interface PrenotazioneService extends BaseRestService<PrenotazioneDTO> {

    List<DisponibilitaPrenotazioneDTO> trovaDisponibili(CustomUserPrincipalDTO utenteCorrente, LocalDate data);

    RiepilogoStanzaDTO riepilogoStanza(CustomUserPrincipalDTO utenteCorrente, Long id, LocalDate data);

    List<PrenotazioneDTO> trovaPrenotate(CustomUserPrincipalDTO utenteCorrente, LocalDate data);

    List<PrenotazioneDTO> trovaMiePrenotazioni(CustomUserPrincipalDTO utenteCorrente, LocalDate data);

    MessageResponse creaPrenotazione(CustomUserPrincipalDTO utenteCorrente, RichiestaPrenotazioneDTO richiesta);

    void eliminaPrenotazione(CustomUserPrincipalDTO utenteCorrente, Long idPrenotazione);
}
