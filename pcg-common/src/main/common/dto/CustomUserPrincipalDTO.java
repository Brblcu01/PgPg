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
public class CustomUserPrincipalDTO extends BaseDTO {
	
	private Long id;
    private String username;
    private Long idRole;
    private Long idOrganization;
    private Long idStructure;
    private Long idOffice;
    private Long idProfile;

}
