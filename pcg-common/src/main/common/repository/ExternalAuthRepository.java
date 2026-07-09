package common.repository;

import common.base.BaseRepository;
import common.entity.ExternalAuth;

import java.util.Optional;

public interface ExternalAuthRepository extends BaseRepository<ExternalAuth> {

    Optional<ExternalAuth> findByAuthMethod(String authMethod);

    Optional<ExternalAuth> findByTenantId(String tenantId);
    
}
