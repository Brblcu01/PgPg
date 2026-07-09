package be4fe.controller;

import be4fe.dto.SideMenuDTO;
import be4fe.service.SideMenuService;
import common.base.BaseController;
import common.dto.CustomUserPrincipalDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Side Menu", description = "Menu laterale")
public class SideMenuController extends BaseController<SideMenuService, SideMenuDTO> {

    public SideMenuController(SideMenuService service) {
        super(service);
    }

    @GetMapping("/side-menu")
    public List<SideMenuDTO> getMenu(@AuthenticationPrincipal CustomUserPrincipalDTO principal) {
        return this.service.getMenu(principal.getIdRole());
    }

}
