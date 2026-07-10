package common.repository;

import common.base.BaseRepository;
import common.entity.CfProfile;

public interface CfProfileRepository extends BaseRepository<CfProfile> {

    boolean existsByIdAndCodeIgnoreCase(Long id, String code);
}
