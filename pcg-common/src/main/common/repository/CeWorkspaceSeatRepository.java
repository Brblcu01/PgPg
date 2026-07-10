package common.repository;

import common.base.BaseRepository;
import common.entity.CeWorkspaceSeat;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CeWorkspaceSeatRepository extends BaseRepository<CeWorkspaceSeat> {

    @Query("""
        SELECT p FROM CeWorkspaceSeat p
        WHERE p.id = :idPosto
          AND p.idWorkspaceFk = :idWorkspace
          AND (p.attiva IS NULL OR p.attiva = true)
          AND (p.marcata IS NULL OR p.marcata = false)
    """)
    Optional<CeWorkspaceSeat> trovaAttivoNonMarcatoPerIdEWorkspace(
            @Param("idPosto") Long idPosto,
            @Param("idWorkspace") Long idWorkspace
    );

    @Query("""
        SELECT p FROM CeWorkspaceSeat p
        WHERE p.idWorkspaceFk = :idWorkspace
          AND (p.attiva IS NULL OR p.attiva = true)
          AND (p.marcata IS NULL OR p.marcata = false)
        ORDER BY p.codice ASC
    """)
    List<CeWorkspaceSeat> trovaPostiPerWorkspace(@Param("idWorkspace") Long idWorkspace);


}