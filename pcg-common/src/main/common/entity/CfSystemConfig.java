package common.entity;

import common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CF_SystemConfig")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class CfSystemConfig extends BaseEntity {

    @Column(name = "CfgKey", nullable = false, length = 200)
    private String cfgKey;

    @Column(name = "CfgValue", nullable = false, length = 200)
    private String cfgValue;

    @Column(name = "IsRequired")
    private Boolean isRequired;

    @Column(name = "CreationDate", nullable = false, insertable = false, updatable = false)
    private LocalDateTime creationDate;

    @Column(name = "IsMarked")
    private Boolean isMarked;
   
}