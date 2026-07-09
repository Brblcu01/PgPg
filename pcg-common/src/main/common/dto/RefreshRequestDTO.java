package common.dto;

import common.base.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


@Getter
@Setter@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Richiesta di rinnovo del token tramite refresh token")
public class RefreshRequestDTO extends BaseDTO {

    private String clientId;
    private String clientSecret;
    private String authMethod;
    private String tenantId;
    private String refreshToken;
}
