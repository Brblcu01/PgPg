package sso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequestDTO {

    @NotBlank
    private String tenantId;
    @NotBlank
    private String postLogoutRedirectUri;
    @NotBlank
    private String idToken;
    @NotBlank
    private String authMethod;
}