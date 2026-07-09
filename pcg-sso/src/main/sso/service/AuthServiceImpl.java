package sso.service;

import common.dto.OAuth2RequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sso.dto.AzureTokenResponseDTO;
import sso.dto.LogoutRequestDTO;
import common.dto.RefreshRequestDTO;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AzureAuthServiceImpl azureAuthService;

    @Override
    public Map<String, Object> getProviderLoginUrl(OAuth2RequestDTO oAuth2RequestDTO) {
        log.info("AUTH-SERVICE: Smistamento richiesta per provider: {}", oAuth2RequestDTO.getAuthMethod());

        if ("Azure".equalsIgnoreCase(oAuth2RequestDTO.getAuthMethod())) {
            return azureAuthService.login(oAuth2RequestDTO);
        }
        if ("SPID".equalsIgnoreCase(oAuth2RequestDTO.getAuthMethod())) {
            // return spidAuthService.login(dto);
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Supporto SPID non ancora implementato");
        }

        log.error("AUTH-SERVICE: Metodo {} non riconosciuto", oAuth2RequestDTO.getAuthMethod());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metodo di autenticazione non supportato");
    }

    @Override
    public Object callback(OAuth2RequestDTO oAuth2RequestDTO) {
        if ("Azure".equalsIgnoreCase(oAuth2RequestDTO.getAuthMethod())) {
            return azureAuthService.callback(oAuth2RequestDTO);
        }
        if ("SPID".equalsIgnoreCase(oAuth2RequestDTO.getAuthMethod())) {
            // return spidAuthService.login(dto);
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Supporto SPID non ancora implementato");
        }

        log.error("AUTH-SERVICE: Metodo {} non riconosciuto", oAuth2RequestDTO.getAuthMethod());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metodo di callback non supportato");
    }

    @Override
    public Object refresh(RefreshRequestDTO refreshRequestDTO) {
        if ("Azure".equalsIgnoreCase(refreshRequestDTO.getAuthMethod())) {
            return azureAuthService.refresh(refreshRequestDTO);
        }
        if ("SPID".equalsIgnoreCase(refreshRequestDTO.getAuthMethod())) {
            // return spidAuthService.login(dto);
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Supporto SPID non ancora implementato");
        }

        log.error("AUTH-SERVICE: Metodo {} non riconosciuto", refreshRequestDTO.getAuthMethod());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metodo di refresh non supportato");
    }

    @Override
    public Map<String, Object> logout(LogoutRequestDTO logoutRequestDTO) {
        if ("Azure".equalsIgnoreCase(logoutRequestDTO.getAuthMethod())) {
            return azureAuthService.logout(logoutRequestDTO);
        }
        if ("SPID".equalsIgnoreCase(logoutRequestDTO.getAuthMethod())) {
            // return spidAuthService.login(dto);
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Supporto SPID non ancora implementato");
        }

        log.error("AUTH-SERVICE: Metodo {} non riconosciuto", logoutRequestDTO.getAuthMethod());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metodo di logout non supportato");
    }

    @Override
    public AzureTokenResponseDTO getGraphToken(OAuth2RequestDTO oAuth2RequestDTO) {
        if ("Azure".equalsIgnoreCase(oAuth2RequestDTO.getAuthMethod())) {
            return azureAuthService.getGraphToken(oAuth2RequestDTO);
        }
        if ("SPID".equalsIgnoreCase(oAuth2RequestDTO.getAuthMethod())) {
            // return spidAuthService.login(dto);
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Supporto SPID non ancora implementato");
        }

        log.error("AUTH-SERVICE: Metodo {} non riconosciuto", oAuth2RequestDTO.getAuthMethod());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metodo di logout non supportato");
    }


}
