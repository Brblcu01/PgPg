package be4fe.mapper;

import be4fe.dto.PrenotazioneDTO;
import common.base.BaseGenericConverter;
import common.dto.CustomUserPrincipalDTO;
import common.entity.CeBooking;
import common.entity.CeWorkspace;
import org.springframework.stereotype.Component;

@Component
public class PrenotazioneConverter extends BaseGenericConverter<CeBooking, PrenotazioneDTO> {

    @Override
    public PrenotazioneDTO toDTO(CeBooking prenotazione) {
        return toDTO(prenotazione, null);
    }

    public PrenotazioneDTO toDTO(CeBooking prenotazione, CustomUserPrincipalDTO user) {
        CeWorkspace postazioneLavoro = prenotazione.getPostazioneLavoro();

        return PrenotazioneDTO.builder()
                .idPrenotazione(prenotazione.getId())
                .idWorkspace(prenotazione.getIdRisorsaPrenotabileFk())
                .codiceWorkspace(postazioneLavoro != null ? postazioneLavoro.getCodice() : null)
                .nomeWorkspace(postazioneLavoro != null ? postazioneLavoro.getNome() : null)
                .idUtente(prenotazione.getIdUtenteFk())
                .utenteName(user.getUsername())
                .idPostazione(prenotazione.getPostazioneLavoro().getId())
                .postazioneName(prenotazione.getPostazioneLavoro().getNome())
                .dataPrenotazione(prenotazione.getDataPrenotazione())
                .stato(prenotazione.getStato())
                .dataCreazione(prenotazione.getDataCreazione())
                .prenotazioneUtenteCorrente(prenotazioneUtenteCorrente(prenotazione, user))
                .build();
    }

    @Override
    public CeBooking toEntity(PrenotazioneDTO dto) {
        CeBooking entity = new CeBooking();
        entity.setId(dto.getIdPrenotazione());
        entity.setIdRisorsaPrenotabileFk(dto.getIdWorkspace());
        entity.setIdUtenteFk(dto.getIdUtente());
        entity.setDataPrenotazione(dto.getDataPrenotazione());
        entity.setStato(dto.getStato());
        entity.setDataCreazione(dto.getDataCreazione());
        return entity;
    }

    @Override
    public CeBooking updateEntity(PrenotazioneDTO source, CeBooking target) {
        target.setIdRisorsaPrenotabileFk(source.getIdWorkspace());
        target.setIdUtenteFk(source.getIdUtente());
        target.setDataPrenotazione(source.getDataPrenotazione());
        target.setStato(source.getStato());
        target.setDataCreazione(source.getDataCreazione());
        return target;
    }

    private boolean prenotazioneUtenteCorrente(CeBooking prenotazione, CustomUserPrincipalDTO user) {
        return user != null
                && user.getId() != null
                && user.getId().equals(prenotazione.getIdUtenteFk());
    }
}
