package common.repository;

import common.base.BaseRepository;
import common.entity.CfSystemConfig;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CfSystemConfigRepository extends BaseRepository<CfSystemConfig> {

    @Query(value = """
    SELECT *
    FROM CF_SystemConfig sc
    WHERE sc.CfgKey IN ('OrganizationalStructure', 'role', 'office')
    AND sc.IsMarked IS NULL
    """, nativeQuery = true)
    List<CfSystemConfig> getConfigurations();

    Optional<CfSystemConfig> findByCfgKey(String cfgKey);
}
