package game;

public class InvalidMoveException extends Exception {
    private static final long serialVersionUID = 2818514884939127529L;

    public InvalidMoveException(String message) {
        super(message);
    }

    public InvalidMoveException(String message, Throwable error) {
        super(message, error);
    }
}
