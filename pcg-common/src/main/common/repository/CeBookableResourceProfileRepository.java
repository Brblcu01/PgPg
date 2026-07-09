package common.repository;

import common.base.BaseRepository;
import common.entity.CeBookableResourceProfile;

public interface CeBookableResourceProfileRepository extends BaseRepository<CeBookableResourceProfile> {

    boolean existsByIdRisorsaPrenotabileFkAndIdProfiloFk(Long idRisorsaPrenotabileFk, Long idProfiloFk);
}
