package be4fe.dto;

import common.base.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RichiestaPrenotazioneDTO extends BaseDTO {

    private Long idRisorsaPrenotabile;
    private LocalDate dataPrenotazione;
}
