package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import common.base.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphUserInfoDTO extends BaseDTO {
    private String id;             // Azure AD Object ID → ExternalIdentity
    private String displayName;
    private String givenName;      // FirstName
    private String surname;        // LastName
    private String department;     // → OrganizationalStructure
    private String officeLocation; // → Office
    private String jobTitle;       // → Role (se mappato su titolo)
}