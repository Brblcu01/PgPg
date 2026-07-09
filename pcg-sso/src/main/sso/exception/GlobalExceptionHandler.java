package sso.exception;

import common.model.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGenericException (Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(getMessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<MessageResponse> handle4xx (HttpClientErrorException ex) {
        log.error("Errore 4xx dal servizio SSO", ex);
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(getMessageResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString()));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<MessageResponse> handle5xx (HttpServerErrorException ex) {
        log.error("Errore 5xx dal servizio contattato: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(getMessageResponse(HttpStatus.BAD_GATEWAY.value(), "Errore 5xx dal servizio contattato"));
    }

    @ExceptionHandler(EmptyAzureResponseException.class)
        public MessageResponse handleBadRequest (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(getMessageResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            ex.getMessage()
                    )).getBody();
    }

    private MessageResponse getMessageResponse(int httpStatus, String message) {
        return new MessageResponse(
                httpStatus,
                message,
                LocalDateTime.now()
        );
    }
}
