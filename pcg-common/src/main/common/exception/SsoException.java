package common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Eccezione lanciata quando il microservizio SSO risponde con un errore
 * o non è raggiungibile durante una chiamata HTTP da BE4FE.
 */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class SsoException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SsoException(String message) {
        super(message);
    }

    public SsoException(String message, Throwable cause) {
        super(message, cause);
    }
}
