package be4fe.dto;

import java.util.List;

import common.base.BaseDTO;
import lombok.Data;

@Data
public class SideMenuDTO extends BaseDTO {
	
	private String label;
    private String icon;
    private String routerLink;
    private List<SideMenuDTO> items;

}
