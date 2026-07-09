package common.entity;

import common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CE_Office", schema = "dbo")
@Getter
@Setter
@AttributeOverride(
        name = "id",
        column = @Column(name = "IdOffice")
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CeOffice extends BaseEntity {

    @Column(name = "IdOrganizationalStructure_Fk")
    private Long idOrganizationalStructureFk;

    @Column(name = "Code")
    private String code;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdOrganizationalStructure_Fk", referencedColumnName = "IdOrganizationalStructure", insertable = false, updatable = false)
    private CeOrganizationalStructure organizationalStructure;
}