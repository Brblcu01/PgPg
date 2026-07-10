package common.repository;

import common.base.BaseRepository;
import common.entity.CeBookingBlock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CeBookingBlockRepository extends BaseRepository<CeBookingBlock> {

    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM CeBookingBlock b
        WHERE (b.marcata IS NULL OR b.marcata = false)
          AND b.dataInizio <= :data
          AND b.dataFine >= :data
    """)
    boolean existsBloccoAttivo(@Param("data") LocalDate data);

    @Query("""
        SELECT b FROM CeBookingBlock b
        WHERE (b.marcata IS NULL OR b.marcata = false)
          AND (
              (:data IS NOT NULL AND b.dataInizio <= :data AND b.dataFine >= :data)
              OR
              (:data IS NULL
                  AND (:dataDa IS NULL OR b.dataFine >= :dataDa)
                  AND (:dataA IS NULL OR b.dataInizio <= :dataA)
              )
          )
        ORDER BY b.dataInizio DESC, b.dataFine DESC
    """)
    List<CeBookingBlock> trovaBlocchiAttivi(
            @Param("data") LocalDate data,
            @Param("dataDa") LocalDate dataDa,
            @Param("dataA") LocalDate dataA
    );
}
