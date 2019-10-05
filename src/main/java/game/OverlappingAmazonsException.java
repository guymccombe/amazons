package game;

public class OverlappingAmazonsException extends Exception {

    public OverlappingAmazonsException(String message) {
        super(message);
    }

    public OverlappingAmazonsException(String message, Throwable error) {
        super(message, error);
    }
}