package game;

public class PointOutOfBoundsException extends Exception {

    public PointOutOfBoundsException(String message) {
        super(message);
    }

    public PointOutOfBoundsException(String message, Throwable error) {
        super(message, error);
    }

    public PointOutOfBoundsException(PointInterface point) {
        super("Point: " + point.toString() + " not in board.");
    }

    public PointOutOfBoundsException(PointInterface point, Throwable error) {
        super("Point: " + point.toString() + " not in board.", error);
    }
}
