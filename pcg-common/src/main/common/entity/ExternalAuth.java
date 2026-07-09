package common.entity;

import common.base.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AttributeOverride(
        name = "id",
        column = @Column(name = "IdExternalAuth")
)
@Getter
@Setter
@Table(name = "CF_ExternalAuth")
public class ExternalAuth extends BaseEntity {

    @Column(name = "Name")
    private String name;

    @Column(name = "Env")
    private String env;

    @Column(name = "AuthMethod")
    private String authMethod;

    @Column(name = "ClientID")
    private String clientID;

    @Column(name = "TenantID")
    private String tenantId;

    @Column(name = "ClientSecret")
    private String clientSecret;

    @Column(name = "RedirectURI")
    private String redirectURI;

    @Column(name = "RedirectURI_Local")
    private String redirectURILocal;

    @Column(name = "ClientRedirectURL")
    private String clientRedirectUrl;

    @Column(name = "CreationDate")
    private LocalDateTime creationDate;
}