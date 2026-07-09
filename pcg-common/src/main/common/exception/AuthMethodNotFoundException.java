package common.exception;

public class AuthMethodNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthMethodNotFoundException(String authMethod) {
        super("Metodo scelto non presente nel sistema: " + authMethod);
    }
}
