package common.entity;

import common.base.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import java.util.Date;

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

    @Column(name = "Env", columnDefinition = "nchar(1)")
    private String env;

    @Column(name = "AuthMethod")
    private String authMethod;

    @Column(name = "ClientID", columnDefinition = "varbinary")
    @ColumnTransformer(read = "CONVERT(varchar(255), DecryptByPassPhrase('Pr0cGr!12%', ClientID))")
    private String clientID;

    @Column(name = "TenantID", columnDefinition = "varbinary")
    @ColumnTransformer(read = "CONVERT(varchar(255), DecryptByPassPhrase('Pr0cGr!12%', TenantID))")
    private String tenantId;

    @Column(name = "ClientSecret", columnDefinition = "varbinary")
    @ColumnTransformer(read = "CONVERT(varchar(255), DecryptByPassPhrase('Pr0cGr!12%', ClientSecret))")
    private String clientSecret;

    @Column(name = "RedirectURI", columnDefinition = "varbinary")
    @ColumnTransformer(read = "CONVERT(varchar(255), DecryptByPassPhrase('Pr0cGr!12%', RedirectURI))")
    private String redirectURI;

    @Column(name = "RedirectURI_Local", columnDefinition = "varbinary")
    @ColumnTransformer(read = "CONVERT(varchar(255), DecryptByPassPhrase('Pr0cGr!12%', RedirectURI_Local))")
    private String redirectURILocal;

    @Column(name = "ClientRedirectURL", columnDefinition = "varbinary")
    @ColumnTransformer(read = "CONVERT(varchar(255), DecryptByPassPhrase('Pr0cGr!12%', ClientRedirectURL))")
    private String clientRedirectUrl;

    @Column(name = "CreationDate")
    private Date creationDate;

}