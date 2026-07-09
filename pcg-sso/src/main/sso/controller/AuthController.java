package sso.controller;

import common.dto.OAuth2RequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sso.dto.AzureTokenResponseDTO;
import sso.dto.LogoutRequestDTO;
import common.dto.RefreshRequestDTO;
import sso.service.AuthService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/provider_login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody OAuth2RequestDTO request) throws Exception {
        return ResponseEntity.ok(authService.getProviderLoginUrl(request));
    }

    @PostMapping("/callback")
    public ResponseEntity<?> callback(@Valid @RequestBody OAuth2RequestDTO request) {
        return ResponseEntity.ok(authService.callback(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequestDTO request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@Valid @RequestBody LogoutRequestDTO request) throws Exception {
        return ResponseEntity.ok(authService.logout(request));
    }

    @PostMapping("/graph-token")
    public ResponseEntity<Map<String, Object>> graphToken(@RequestBody OAuth2RequestDTO dto) {
        AzureTokenResponseDTO token = authService.getGraphToken(dto);
        Map<String, Object> resp = new HashMap<>();
        resp.put("accessToken", token.getAccessToken());
        return ResponseEntity.ok(resp);
    }

}