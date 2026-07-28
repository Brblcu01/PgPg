package be4fe.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import common.base.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RichiestaPrenotazioneDTO extends BaseDTO {

    private Long idWorkspace;
    private LocalDate dataPrenotazione;
    private Long idWorkspaceSeat;

    @JsonAlias("start")
    private LocalTime hourStart;

    @JsonAlias("end")
    private LocalTime hourEnd;
}
