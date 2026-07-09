package common.base;

import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@MappedSuperclass
public abstract class BaseDTO {
}
