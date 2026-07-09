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
                .idRisorsaPrenotabile(prenotazione.getIdRisorsaPrenotabileFk())
                .codiceRisorsa(postazioneLavoro != null ? postazioneLavoro.getCodice() : null)
                .nomeRisorsa(postazioneLavoro != null ? postazioneLavoro.getNome() : null)
                .idUtente(prenotazione.getIdUtenteFk())
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
        entity.setIdRisorsaPrenotabileFk(dto.getIdRisorsaPrenotabile());
        entity.setIdUtenteFk(dto.getIdUtente());
        entity.setDataPrenotazione(dto.getDataPrenotazione());
        entity.setStato(dto.getStato());
        entity.setDataCreazione(dto.getDataCreazione());
        return entity;
    }

    @Override
    public CeBooking updateEntity(PrenotazioneDTO source, CeBooking target) {
        target.setIdRisorsaPrenotabileFk(source.getIdRisorsaPrenotabile());
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
