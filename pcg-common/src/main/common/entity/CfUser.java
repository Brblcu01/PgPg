package common.entity;

import common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CF_User", schema = "dbo")
@AttributeOverride(
		name = "id",
		column = @Column(name = "IdUser")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CfUser extends BaseEntity {

	@Column(name = "Username", length = 64, unique = true)
	private String username;

	@Column(name = "Code_Ext", length = 20)
	private String codeExt;

	@Column(name = "IdRole_Fk")
	private Long idRoleFk;
	
	@Column(name = "IdProfile_Fk")
	private Long idProfileFk;
	

	@Column(name = "IdRegistry_Fk")
	private Long idRegistryFk;

	@Column(name = "IdLanguage_Fk")
	private Long idLanguageFk;

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(
//			name = "IdUserType_Fk",
//			referencedColumnName = "IdUserType",
//			insertable = false,
//			updatable = false
//	)
//	private LkCfgUserType userType;

	@Column(name = "IdUserType_Fk")
	private Long idUserTypeFk;

	@Column(name = "IdCurrency_Fk")
	private Long idCurrencyFk;

	@Column(name = "IdPicture_Fk")
	private Long idPictureFk;

	@Column(name = "Email", length = 100)
	private String email;

	@Column(name = "FirstName")
	private String firstName;

	@Column(name = "LastName")
	private String lastName;

	@Column(name = "IdOrganizationalStructure_Fk")
	private Long idOrganizationalStructureFk;

	@Column(name = "IdOffice_Fk")
	private Long idOfficeFk;

	@Column(name = "CreationDate")
	private LocalDateTime creationDate;

	@Column(name = "IsMarked")
	private Boolean isMarked;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "IdOrganizationalStructure_Fk",
			referencedColumnName = "IdOrganizationalStructure",
			insertable = false,
			updatable = false
	)
	private CeOrganizationalStructure organizationalStructure;

	@Column(name = "IsActive")
	private Boolean isActive = true;

	@Column(name = "LastLoginDate")
	private LocalDateTime lastLoginDate;

	@Column(name = "ExternalIdentity", length = 150)
	private String externalIdentity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "IdOffice_Fk",
			referencedColumnName = "IdOffice",
			insertable = false,
			updatable = false
	)
	private CeOffice office;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "IdRole_Fk",
			referencedColumnName = "IdRole",
			insertable = false,
			updatable = false
	)
	private CfRole role;
}