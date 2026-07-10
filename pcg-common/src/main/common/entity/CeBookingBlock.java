package common.entity;

import common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CE_BookingBlock")
@AttributeOverride(
        name = "id",
        column = @Column(name = "IdBookingBlock")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CeBookingBlock extends BaseEntity {

    @Column(name = "StartDate", nullable = false)
    private LocalDate dataInizio;

    @Column(name = "EndDate", nullable = false)
    private LocalDate dataFine;

    @Column(name = "Reason")
    private String motivo;

    @Column(name = "IdCreatedByUser_Fk")
    private Long idUtenteCreazioneFk;

    @Column(name = "CreationDate")
    private LocalDateTime dataCreazione;

    @Column(name = "LastUpdateDate")
    private LocalDateTime dataUltimoAggiornamento;

    @Column(name = "IsMarked")
    private Boolean marcata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdCreatedByUser_Fk", referencedColumnName = "IdUser", insertable = false, updatable = false)
    private CfUser utenteCreazione;
}