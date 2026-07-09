package common.entity;

import common.base.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "CE_BookableResourceProfile")
@AttributeOverride(
        name = "id",
        column = @Column(name = "IdBookableResourceProfile")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CeBookableResourceProfile extends BaseEntity {

    @Column(name = "IdWorkspace_Fk", nullable = false)
    private Long idRisorsaPrenotabileFk;

    @Column(name = "IdProfile_Fk", nullable = false)
    private Long idProfiloFk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdWorkspace_Fk", referencedColumnName = "IdWorkspace", insertable = false, updatable = false)
    private CeWorkspace postazioneLavoro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdProfile_Fk", referencedColumnName = "IdProfile", insertable = false, updatable = false)
    private CfProfile profilo;
}
