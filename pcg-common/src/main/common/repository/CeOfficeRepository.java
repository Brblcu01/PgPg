package common.repository;

import common.entity.CeOffice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CeOfficeRepository extends JpaRepository<CeOffice, Long> {

    Optional<CeOffice> findByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCaseAndIdOrganizationalStructureFk(
            String code, Long idOrganizationalStructureFk);

    List<CeOffice> findByIdOrganizationalStructureFkOrderByNameAsc(
            Long idOrganizationalStructureFk);
}