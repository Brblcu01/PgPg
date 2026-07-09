package be4fe.dto;

import common.base.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrenotazioneDTO extends BaseDTO {

    private Long idPrenotazione;
    private Long idRisorsaPrenotabile;
    private String codiceRisorsa;
    private String nomeRisorsa;
    private Long idUtente;
    private LocalDate dataPrenotazione;
    private String stato;
    private LocalDateTime dataCreazione;
    private Boolean prenotazioneUtenteCorrente;
}
