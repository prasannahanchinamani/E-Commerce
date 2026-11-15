package customDesign.E_commerce.Exceptions;

public class UserAlreadyFound extends RuntimeException {
    public UserAlreadyFound(String message) {
        super(message);
    }
}
