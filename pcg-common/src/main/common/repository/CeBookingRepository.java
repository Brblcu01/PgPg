package common.repository;

import common.base.BaseRepository;
import common.entity.CeBooking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CeBookingRepository extends BaseRepository<CeBooking> {

    @Query("""
        SELECT COUNT(p) FROM CeBooking p
        WHERE p.idRisorsaPrenotabileFk = :idRisorsa
          AND p.dataPrenotazione = :data
          AND p.stato = :stato
          AND (p.marcata IS NULL OR p.marcata = false)
    """)
    long contaPrenotazioniConfermate(
            @Param("idRisorsa") Long idRisorsa,
            @Param("data") LocalDate data,
            @Param("stato") String stato
    );

    @Query("""
        SELECT COUNT(p) FROM CeBooking p
        WHERE p.idUtenteFk = :idUtente
          AND p.dataPrenotazione = :data
          AND p.stato = :stato
          AND (p.marcata IS NULL OR p.marcata = false)
    """)
    long contaPrenotazioniUtenteConfermatePerData(
            @Param("idUtente") Long idUtente,
            @Param("data") LocalDate data,
            @Param("stato") String stato
    );

    @Query("""
        SELECT p FROM CeBooking p
        JOIN FETCH p.postazioneLavoro r
        WHERE p.dataPrenotazione = :data
          AND p.stato = :stato
          AND (p.marcata IS NULL OR p.marcata = false)
        ORDER BY r.nome ASC, p.dataCreazione ASC
    """)
    List<CeBooking> trovaPrenotazioniConfermate(
            @Param("data") LocalDate data,
            @Param("stato") String stato
    );

    @Query("""
        SELECT p FROM CeBooking p
        JOIN FETCH p.postazioneLavoro r
        WHERE p.idUtenteFk = :idUtente
          AND (:data IS NULL OR p.dataPrenotazione = :data)
          AND (p.marcata IS NULL OR p.marcata = false)
        ORDER BY p.dataPrenotazione DESC, p.dataCreazione DESC
    """)
    List<CeBooking> trovaPrenotazioniUtente(
            @Param("idUtente") Long idUtente,
            @Param("data") LocalDate data
    );

    Optional<CeBooking> findByIdAndIdUtenteFk(Long id, Long idUtenteFk);
}
