package common.entity;

import common.base.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CE_OrganizationalStructure", schema = "dbo")
@AttributeOverride(
        name = "id",
        column = @Column(name = "IdOrganizationalStructure")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CeOrganizationalStructure extends BaseEntity {

    @Column(name = "Code")
    private String code;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "IdStructureType_Fk")
    private Long idStructureTypeFk;

    @Column(name = "IdParentStructure_Fk")
    private Long idParentStructureFk;

    @Column(name = "IsMinisterOffice")
    private Boolean isMinisterOffice;

    @Column(name = "WebsiteUrl")
    private String websiteUrl;

    @Column(name = "ValidFrom")
    private LocalDateTime validFrom;

    @Column(name = "ValidTo")
    private LocalDateTime validTo;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "CreationDate")
    private LocalDateTime creationDate;

    @Column(name = "IsMarked")
    private Boolean isMarked;

    @Column(name = "LastUpdateDate")
    private LocalDateTime lastUpdateDate;
}