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
public class BloccoPrenotazioniDTO extends BaseDTO {

    private Long idBlocco;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private String motivo;
    private Long idUtenteCreazione;
    private LocalDateTime dataCreazione;
}
