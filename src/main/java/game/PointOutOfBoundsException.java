package game;

public class PointOutOfBoundsException extends Exception {

    public PointOutOfBoundsException(String message) {
        super(message);
    }

    public PointOutOfBoundsException(String message, Throwable error) {
        super(message, error);
    }
}