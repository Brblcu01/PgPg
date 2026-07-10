package be4fe.dto;

import common.base.BaseDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostoWorkspaceDTO extends BaseDTO {

    private Long idWorkspaceSeat;
    private Long idWorkspace;
    private String codice;
    private String nome;
    private Boolean occupato;
}