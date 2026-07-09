package common.repository;

import common.base.BaseRepository;
import common.entity.CeWorkspace;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CeWorkspaceRepository extends BaseRepository<CeWorkspace> {

    @Query("""
        SELECT r FROM CeWorkspace r
        WHERE (r.attiva IS NULL OR r.attiva = true)
          AND (r.marcata IS NULL OR r.marcata = false)
        ORDER BY r.nome ASC
    """)
    List<CeWorkspace> trovaTuttePrenotabili();

    @Query("""
    SELECT r FROM CeWorkspace r
    WHERE r.id = :id
      AND (r.attiva IS NULL OR r.attiva = true)
      AND (r.marcata IS NULL OR r.marcata = false)
""")
    Optional<CeWorkspace> trovaAttivaNonMarcataPerId(@Param("id") Long id);
}
