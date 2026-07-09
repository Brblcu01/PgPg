package common.entity;

import common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "CF_AppFunctionArea")
public class CfAppFunctionArea extends BaseEntity {

    @Column(name = "idappfunctionarea_parent_fk")
    private Long idAppFunctionAreaParentFk;

    @Column(name = "idrole_fk")
    private Long idRoleFk;

    @Column(name = "idlanguage_fk")
    private Long idLanguageFk;

    @Column(name = "idpicture_fk")
    private Long idPictureFk;

    @Column(name = "icon")
    private String icon;

    @Column(name = "routerlink")
    private String routerLink;

    @Column(name = "name")
    private String name;

    @Column(name = "commonname")
    private String commonName;

    @Column(name = "creationdate")
    private LocalDateTime creationDate;

    @Column(name = "ismarked")
    private Boolean isMarked;

}

