package common.entity;

import common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CE_WorkspaceSeat")
@AttributeOverride(
        name = "id",
        column = @Column(name = "IdWorkspaceSeat")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CeWorkspaceSeat extends BaseEntity {

    @Column(name = "IdWorkspace_Fk", nullable = false)
    private Long idWorkspaceFk;

    @Column(name = "Code", nullable = false, length = 50)
    private String codice;

    @Column(name = "Name", length = 100)
    private String nome;

    @Column(name = "IsActive")
    private Boolean attiva;

    @Column(name = "CreationDate")
    private LocalDateTime dataCreazione;

    @Column(name = "LastUpdateDate")
    private LocalDateTime dataUltimoAggiornamento;

    @Column(name = "IsMarked")
    private Boolean marcata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdWorkspace_Fk", referencedColumnName = "IdWorkspace", insertable = false, updatable = false)
    private CeWorkspace workspace;
}
