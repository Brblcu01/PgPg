package common.repository;

import common.base.BaseRepository;
import common.entity.CfAppFunctionArea;

import java.util.List;

public interface CfAppFunctionAreaRepository extends BaseRepository<CfAppFunctionArea> {
	
	List<CfAppFunctionArea> findByIdRoleFk(Long idRole);

}
