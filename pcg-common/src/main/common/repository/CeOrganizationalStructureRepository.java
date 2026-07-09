package common.repository;

import common.base.BaseRepository;
import common.entity.CeOrganizationalStructure;

import java.util.Optional;

public interface CeOrganizationalStructureRepository extends BaseRepository<CeOrganizationalStructure> {

    Optional<CeOrganizationalStructure> findByNameIgnoreCase(String name);


}
