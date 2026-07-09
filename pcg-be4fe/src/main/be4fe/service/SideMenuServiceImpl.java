package be4fe.service;

import be4fe.dto.SideMenuDTO;
import common.base.BaseGenericRestService;
import common.entity.CfAppFunctionArea;
import common.repository.CfAppFunctionAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SideMenuServiceImpl extends BaseGenericRestService<CfAppFunctionArea, SideMenuDTO, CfAppFunctionAreaRepository>
		implements SideMenuService {

    @Override
	public List<SideMenuDTO> getMenu(Long idRole) {

	    List<CfAppFunctionArea> functions = this.repository.findByIdRoleFk(idRole);

	    Map<Long, SideMenuDTO> map = new HashMap<>();
	    List<SideMenuDTO> roots = new ArrayList<>();

	    for (CfAppFunctionArea f : functions) {

	        SideMenuDTO dto = new SideMenuDTO();
	        dto.setLabel(f.getCommonName());
	        dto.setIcon(f.getIcon());
	        dto.setRouterLink(f.getRouterLink());
	        dto.setItems(new ArrayList<>());

	        map.put(f.getId(), dto);
	    }

	    // collego padre -> figli
	    for (CfAppFunctionArea f : functions) {

	        SideMenuDTO dto = map.get(f.getId());

	        Long parentId = f.getIdAppFunctionAreaParentFk();

	        if (parentId == 0) {
	            roots.add(dto);
	        } else {
	            SideMenuDTO parent = map.get(parentId);
	            if (parent != null) {
	                parent.getItems().add(dto);
	            }
	        }
	    }

	    return roots;
	}

}
