package common.dto;


import common.base.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GraphTokenRequestDTO extends BaseDTO {

    @NotBlank
    private String authMethod;

}
