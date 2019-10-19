package game;

public class GameInProgressException extends Exception {

    public GameInProgressException(String message) {
        super(message);
    }

    public GameInProgressException(String message, Throwable error) {
        super(message, error);
    }
}
