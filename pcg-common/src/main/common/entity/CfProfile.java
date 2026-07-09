package common.entity;

import common.base.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CF_Profile")
@AttributeOverride(
        name = "id",
        column = @Column(name = "IdProfile")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CfProfile extends BaseEntity {

    @Column(name = "Code", nullable = false, length = 50)
    private String code;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "CreationDate")
    private LocalDateTime creationDate;

    @Column(name = "IsMarked")
    private Boolean isMarked;
}
