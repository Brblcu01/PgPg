package common.dto;

import common.base.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2RequestDTO extends BaseDTO {

    @NotBlank
    private String authMethod; //Spid o Azure

    @NotBlank
    private String clientId;

    @NotBlank
    private String redirectUri;

    @NotBlank
    private String tenantId;

    private String state;
    private String clientSecret;
    private String code;
    private String scope;
}