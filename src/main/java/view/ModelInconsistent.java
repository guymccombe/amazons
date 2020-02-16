public class ModelInconsistent extends Exception {

    public ModelInconsistent(String message) {
        super(message);
    }

    public ModelInconsistent(String message, Throwable error) {
        super(message, error);
    }
}
