package be4fe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Risposta al login: contiene l'URL di redirect verso il provider SSO")
public class LoginResponseDTO {

	/**
	 * DTO della risposta alla chiamata di login.
	 *
	 * <p>Quando il frontend chiama {@code POST /auth/login}, BE4FE risponde
	 * con l'URL di autorizzazione verso cui il browser deve essere reindirizzato
	 * per completare il login tramite il provider esterno (Azure AD, SPID, ecc.).</p>
	 *
	 * <p>Il frontend deve eseguire {@code window.location.href = authorizationUrl}
	 * per avviare il redirect verso il provider.</p>
	 */
	
    /**
     * URL di autorizzazione costruito dall'SSO verso cui il browser
     * dell'utente deve essere reindirizzato per completare il login.
     * Esempio: {@code https://login.microsoftonline.com/tenant-id/oauth2/v2.0/authorize?...}
     */
    @Schema(description = "URL di redirect verso il provider esterno (Azure AD / SPID)",
            example = "https://login.microsoftonline.com/.../oauth2/v2.0/authorize?client_id=...&redirect_uri=...")
    private String authorizationUrl;

    /**
     * Provider identificato per questo dominio email.
     * Valori possibili: {@code AZURE}, {@code SPID}.
     */
    @Schema(description = "Provider identificato", example = "AZURE")
    private String provider;
}
