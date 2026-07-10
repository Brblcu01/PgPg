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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CE_Booking")
@AttributeOverride(
        name = "id",
        column = @Column(name = "IdBooking")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CeBooking extends BaseEntity {

    @Column(name = "IdWorkspace_Fk", nullable = false)
    private Long idRisorsaPrenotabileFk;

    @Column(name = "IdUser_Fk", nullable = false)
    private Long idUtenteFk;

    @Column(name = "BookingDate", nullable = false)
    private LocalDate dataPrenotazione;

    @Column(name = "Status", nullable = false, length = 20)
    private String stato;

    @Column(name = "CreationDate")
    private LocalDateTime dataCreazione;

    @Column(name = "LastUpdateDate")
    private LocalDateTime dataUltimoAggiornamento;

    @Column(name = "IsMarked")
    private Boolean marcata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdWorkspace_Fk", referencedColumnName = "IdWorkspace", insertable = false, updatable = false)
    private CeWorkspace postazioneLavoro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUser_Fk", referencedColumnName = "IdUser", insertable = false, updatable = false)
    private CfUser utente;

    @Column(name = "IdWorkspaceSeat_Fk")
    private Long idWorkspaceSeatFk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdWorkspaceSeat_Fk", referencedColumnName = "IdWorkspaceSeat", insertable = false, updatable = false)
    private CeWorkspaceSeat posto;
}
