package common.dto;

import common.base.BaseDTO;
import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class LogoutRequestDTO extends BaseDTO {

    private String clientId;
    private String clientSecret;
    private String token;
    private String tokenTypeHint;
    private String refreshToken;
}
