package common.repository;

import common.base.BaseRepository;
import common.entity.CfRole;

import java.util.Optional;

public interface CfRoleRepository extends BaseRepository<CfRole> {

    Optional<CfRole> findByNameIgnoreCase(String name);

}
