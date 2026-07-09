package common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Eccezione lanciata quando il parametro {@code state} ricevuto nella callback OAuth2
 * non può essere decifrato o risulta malformato.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStateException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidStateException(String message) {
        super(message);
    }

    public InvalidStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
