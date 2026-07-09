package be4fe.controller;

import be4fe.dto.LoginRequestDTO;
import be4fe.dto.RefreshDTO;
import be4fe.dto.UserInfoDTO;
import be4fe.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import common.base.BaseController;
import common.base.BaseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@Tag(name = "authentication-controller", description = "Gestione flussi di autenticazione")
public class AuthController extends BaseController<AuthService, BaseDTO> {

    public AuthController(AuthService service) {
        super(service);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(HttpServletRequest request, HttpServletResponse response,
                                                     @RequestBody LoginRequestDTO loginRequestDTO) {
        Map<String, Object> result = this.service.login(request, response, loginRequestDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> Callback(@RequestParam String code, @RequestParam String state,
                                         HttpServletResponse response) {
        this.service.callback(code, state, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/userInfo")
    public ResponseEntity<UserInfoDTO> handleMicrosoftToken(HttpServletRequest request, @RequestBody String code)
            throws Exception {
        return ResponseEntity.ok(this.service.userInfo(request, code));

    }

    @PostMapping("/soft-logout")
    public void logout(@RequestBody Map<String, Object> logoutPayload,
                       @RequestParam Integer idUser,
                       @RequestParam Integer idRole, HttpServletRequest request) throws JsonProcessingException {
        String refreshToken = (String) logoutPayload.get("refreshToken");
        this.service.logout(idUser, idRole, request, refreshToken);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> getRefreshToken(@RequestBody RefreshDTO refreshDTO) {
        Map<String, Object> tokens = this.service.getRefreshToken(refreshDTO);
        return ResponseEntity.ok(tokens);
    }
}
