package common.exception;

import common.model.MessageResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthMethodNotFoundException.class)
    public ResponseEntity<MessageResponse> handleDomainNotFound(AuthMethodNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SsoException.class)
    public ResponseEntity<MessageResponse> handleSsoException(SsoException ex) {
        log.error("Errore SSO: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<MessageResponse> handleInvalidState(InvalidStateException ex) {
        log.warn("State OAuth2 non valido: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        log.warn("Errore di validazione: {}", fieldErrors);
        return buildResponse(HttpStatus.BAD_REQUEST, fieldErrors.toString());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<MessageResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<MessageResponse> handleResponseStatusException(ResponseStatusException ex) {
        return buildResponse(ex.getStatusCode(), ex.getReason());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<MessageResponse> handleHttpClientError(HttpClientErrorException ex) {
        log.error("Errore 4xx dal servizio contattato", ex);
        MessageResponse upstream = ex.getResponseBodyAs(MessageResponse.class);
        return buildResponse(
                ex.getStatusCode(),
                upstream != null ? upstream.getMessage() : "Errore 4xx dal servizio contattato"
        );
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<MessageResponse> handle5xx(HttpServerErrorException ex) {
        log.error("Errore 5xx dal servizio contattato: ", ex);
        return buildResponse(HttpStatus.BAD_GATEWAY, "Errore 5xx dal servizio contattato");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponse> handleRuntimeException(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<MessageResponse> handleInvalidRole(InvalidRoleException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<MessageResponse> buildResponse(HttpStatusCode status, String message) {
        return ResponseEntity
                .status(status)
                .body(new MessageResponse(status.value(), message, LocalDateTime.now()));
    }
}