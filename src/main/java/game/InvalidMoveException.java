package game;

public class InvalidMoveException extends Exception {

    public InvalidMoveException(String message) {
        super(message);
    }

    public InvalidMoveException(String message, Throwable error) {
        super(message, error);
    }
}
