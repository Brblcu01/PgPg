package be4fe.dto;

import lombok.Data;

@Data
public class RefreshDTO {
	
	private String refreshToken;
	private String authMethod;

}
