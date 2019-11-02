package game;

public class AmazonSelectionException extends Exception {
    private static final long serialVersionUID = 1284054083280482137L;

    public AmazonSelectionException(String message) {
        super(message);
    }

    public AmazonSelectionException(String message, Throwable error) {
        super(message, error);
    }
}
