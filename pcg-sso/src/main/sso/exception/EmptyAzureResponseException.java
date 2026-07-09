package sso.exception;

public class EmptyAzureResponseException extends RuntimeException {
  public EmptyAzureResponseException() {
    super("Risposta Azure vuota");
  }
}
