package be4fe.dto;

import common.base.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Informazioni dell'utente autenticato (da SSO /userinfo)")
public class UserInfoDTO extends BaseDTO {

	private Long idUser;
	private String email;
	private String name;
	private Long idRole;
	private String roleName;
    private String accessToken;
    private String refreshToken;
	private Long IdOrganizationalStructure_Fk;
	private String OrganizationalStructureName;
	private Long IdOffice_Fk;
	private String officeName;
	private String authMethod;
	private String roleCode;
	private String descrizioneRuolo;
	private Long idProfile;
	private String profileCode;
}
