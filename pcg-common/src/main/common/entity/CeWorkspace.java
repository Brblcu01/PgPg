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

import java.time.LocalDateTime;

@Entity
@Table(name = "CE_Workspace")
@AttributeOverride(
        name = "id",
        column = @Column(name = "IdWorkspace")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CeWorkspace extends BaseEntity {

    @Column(name = "IdOffice_Fk")
    private Long idUfficioFk;

    @Column(name = "Code", nullable = false, length = 50)
    private String codice;

    @Column(name = "Name", nullable = false, length = 200)
    private String nome;

    @Column(name = "ResourceType", nullable = false, length = 30)
    private String tipoRisorsa;

    @Column(name = "Capacity", nullable = false)
    private Integer capienza;

    @Column(name = "IsExclusiveBooking", nullable = false)
    private Boolean prenotazioneEsclusiva;

    @Column(name = "IsActive")
    private Boolean attiva;

    @Column(name = "CreationDate")
    private LocalDateTime dataCreazione;

    @Column(name = "LastUpdateDate")
    private LocalDateTime dataUltimoAggiornamento;

    @Column(name = "IsMarked")
    private Boolean marcata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdOffice_Fk", referencedColumnName = "IdOffice", insertable = false, updatable = false)
    private CeOffice ufficio;
}
