package common.repository;

import common.base.BaseRepository;
import common.entity.CeBooking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
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
    WHERE (
        (:data IS NOT NULL AND p.dataPrenotazione = :data)
        OR
        (:data IS NULL
            AND (:dataDa IS NULL OR p.dataPrenotazione >= :dataDa)
            AND (:dataA IS NULL OR p.dataPrenotazione <= :dataA)
        )
    )
      AND p.stato = :stato
      AND (p.marcata IS NULL OR p.marcata = false)
    ORDER BY p.dataPrenotazione DESC, r.nome ASC, p.dataCreazione ASC
""")
    List<CeBooking> trovaPrenotazioniConfermate(
            @Param("data") LocalDate data,
            @Param("dataDa") LocalDate dataDa,
            @Param("dataA") LocalDate dataA,
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

    @Query("""
    SELECT p FROM CeBooking p
    JOIN FETCH p.postazioneLavoro r
    JOIN p.utente u
    JOIN u.profile profilo
    WHERE (
        (:data IS NOT NULL AND p.dataPrenotazione = :data)
        OR
        (:data IS NULL
            AND (:dataDa IS NULL OR p.dataPrenotazione >= :dataDa)
            AND (:dataA IS NULL OR p.dataPrenotazione <= :dataA)
        )
    )
      AND UPPER(profilo.code) = UPPER(:profileCode)
      AND (p.marcata IS NULL OR p.marcata = false)
    ORDER BY p.dataPrenotazione DESC, r.nome ASC, p.dataCreazione DESC
""")
    List<CeBooking> trovaPrenotazioniAdmin(
            @Param("data") LocalDate data,
            @Param("dataDa") LocalDate dataDa,
            @Param("dataA") LocalDate dataA,
            @Param("profileCode") String profileCode
    );

    Optional<CeBooking> findByIdAndIdUtenteFk(Long id, Long idUtenteFk);

    @Query("""
    SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
    FROM CeBooking p
    JOIN p.utente u
    JOIN u.profile profilo
    WHERE p.id = :idPrenotazione
      AND UPPER(profilo.code) = UPPER(:profileCode)
""")
    boolean existsByIdAndUserProfileCode(
            @Param("idPrenotazione") Long idPrenotazione,
            @Param("profileCode") String profileCode
    );

    @Query("""
    SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
    FROM CeBooking p
    WHERE p.idWorkspaceSeatFk = :idPosto
      AND p.dataPrenotazione = :data
      AND p.stato = :stato
      AND (p.marcata IS NULL OR p.marcata = false)
""")
    boolean existsPrenotazioneConfermataPosto(
            @Param("idPosto") Long idPosto,
            @Param("data") LocalDate data,
            @Param("stato") String stato
    );

    @Query("""
    SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
    FROM CeBooking p
    WHERE p.idRisorsaPrenotabileFk = :idWorkspace
      AND p.dataPrenotazione = :data
      AND p.stato = :stato
      AND (p.marcata IS NULL OR p.marcata = false)
      AND p.hourStart < :hourEnd
      AND p.hourEnd > :hourStart
""")
    boolean existsSovrapposizioneSalaRiunioni(
            @Param("idWorkspace") Long idWorkspace,
            @Param("data") LocalDate data,
            @Param("hourStart") LocalTime hourStart,
            @Param("hourEnd") LocalTime hourEnd,
            @Param("stato") String stato
    );
}
