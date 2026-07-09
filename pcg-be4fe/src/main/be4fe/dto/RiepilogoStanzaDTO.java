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
public class RiepilogoStanzaDTO extends BaseDTO {

    private Long idRisorsaPrenotabile;
    private String codice;
    private String nome;
    private Integer postiTotali;
    private Long postiOccupati;
    private Long postiDisponibili;
}
