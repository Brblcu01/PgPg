package be4fe.dto;

import common.base.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilitaPrenotazioneDTO extends BaseDTO {

    private Long idRisorsaPrenotabile;
    private String codice;
    private String nome;
    private String tipoRisorsa;
    private Integer capienza;
    private Boolean prenotazioneEsclusiva;
    private Long prenotazioniConfermate;
    private Long postiDisponibili;
}
