package common.mapper;

import common.dto.*;
import common.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExternalAuthMapper {

	@Mapping(target = "clientId", source = "clientID")
	@Mapping(target = "tenantId", source = "tenantId")
	@Mapping(target = "redirectUri", source = "redirectURI")
	@Mapping(target = "authMethod", source = "authMethod")
	@Mapping(target = "state", ignore = true)
	@Mapping(target = "clientSecret", source = "clientSecret")
	ExternalAuthNoSecretDTO mapNoSecret(ExternalAuth externalAuth);

	@Mapping(target = "clientId", source = "clientID")
	@Mapping(target = "tenantId", source = "tenantId")
	@Mapping(target = "redirectUri", source = "redirectURI")
	@Mapping(target = "clientSecret", source = "clientSecret")
	@Mapping(target = "clientRedirectUrl", expression = "java(externalAuth.getClientRedirectUrl())")
	@Mapping(target = "redirectURILocal", expression = "java(externalAuth.getRedirectURILocal())")
	@Mapping(target = "authMethod", source = "authMethod")
	ExternalAuthWithSecretDTO mapWithSecret(ExternalAuth externalAuth);

	@Mapping(target = "clientId", source = "clientID")
	@Mapping(target = "clientSecret", source = "clientSecret")
	@Mapping(target = "token", ignore = true)
	@Mapping(target = "tokenTypeHint", constant = "access_token")
	@Mapping(target = "refreshToken", constant = "refreshToken")
	LogoutRequestDTO mapLogout(ExternalAuth externalAuth);

	@Mapping(target = "clientId", source = "externalAuth.clientID")
	@Mapping(target = "clientSecret", source = "externalAuth.clientSecret")
	@Mapping(target = "authMethod", source = "externalAuth.authMethod")
	@Mapping(target = "tenantId", source = "externalAuth.tenantId")
	@Mapping(target = "refreshToken", source = "refreshToken")
	RefreshRequestDTO mapRefresh(ExternalAuth externalAuth, String refreshToken);

	@Mapping(target = "clientId", source = "clientID")
	@Mapping(target = "tenantId", source = "tenantId")
	@Mapping(target = "redirectUri", source = "redirectURI")
	@Mapping(target = "clientSecret", source = "clientSecret")
	@Mapping(target = "authMethod", source = "authMethod")
	@Mapping(target = "state", ignore = true)
	@Mapping(target = "code", ignore = true)
	OAuth2RequestDTO mapToOAuth2Request(ExternalAuth externalAuth);

}