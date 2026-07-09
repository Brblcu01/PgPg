package common.dto;

import common.base.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExternalAuthWithSecretDTO extends BaseDTO {
	private String clientId;
	private String tenantId;
	private String redirectUri;
	private String clientSecret;
	private String clientRedirectUrl;
	private String redirectURILocal;
	private String authMethod;

}
