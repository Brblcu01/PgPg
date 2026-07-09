package be4fe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Richiesta di avvio del flusso di autenticazione")
public class LoginRequestDTO {

    /**
     * provider da utilizzare.
     */
    @NotBlank(message = "Il campo authMethod è obbligatorio")
    @Schema(description = "authMethod da utilizzare(Microsoft o SPID)", example = "Azure")
    private String authMethod;

}
