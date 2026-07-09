package common.entity;

import common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(
        name = "id",
        column = @Column(name = "IdRole")
)
@Getter
@Setter
@Entity
@Table(name = "CF_Role")
public class CfRole extends BaseEntity {

    @Column(name = "IdLanguage_Fk", nullable = false)
    private Long idLanguageFk;

    @Column(name = "IdPicture_Fk")
    private Long idPictureFk;
    
    @Column(name = "IdProfile_Fk")
    private Long idProfileFk;

    @Column(name = "Name", nullable = false, length = 200)
    private String name;

    @Column(name = "CreationDate", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime creationDate;

    @Column(name = "IsMarked")
    private Boolean isMarked;

    @Column(name = "Code")
    private String code;

    @Column(name = "Description")
    private String description;

}