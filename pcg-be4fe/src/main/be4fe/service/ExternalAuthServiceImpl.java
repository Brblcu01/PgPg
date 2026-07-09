package be4fe.service;

import common.base.BaseDTO;
import common.base.BaseGenericRestService;
import common.dto.ExternalAuthNoSecretDTO;
import common.dto.ExternalAuthWithSecretDTO;
import common.entity.ExternalAuth;
import common.exception.AuthMethodNotFoundException;
import common.mapper.ExternalAuthMapper;
import common.repository.ExternalAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ExternalAuthServiceImpl extends BaseGenericRestService<ExternalAuth, BaseDTO, ExternalAuthRepository>
        implements ExternalAuthService {

  private final ExternalAuthRepository externalAuthRepository;
  private final ExternalAuthMapper externalAuthMapper;

  @Override
  @Transactional(readOnly = true)
  public ExternalAuthNoSecretDTO getExternalAuthNoSecret(String authMethod) {
    return externalAuthMapper.mapNoSecret(externalAuthRepository.findByAuthMethod(authMethod)
            .orElseThrow(() -> new AuthMethodNotFoundException(authMethod)));

  }

  @Override
  @Transactional(readOnly = true)
  public ExternalAuthWithSecretDTO getExternalAuthWithSecret(String authMethod) {
    return externalAuthMapper.mapWithSecret(externalAuthRepository.findByAuthMethod(authMethod)
            .orElseThrow(() -> new AuthMethodNotFoundException(authMethod)));
  }

  @Override
  @Transactional(readOnly = true)
  public ExternalAuthWithSecretDTO getClientRedirectUrl(String authMethod) {
    return externalAuthMapper.mapWithSecret(externalAuthRepository.findByAuthMethod(authMethod)
            .orElseThrow(() -> new AuthMethodNotFoundException(authMethod)));
  }

}
 