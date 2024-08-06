package sheba.backend.app.exceptions;

public class ImageDeleteFailed extends Exception{
    public ImageDeleteFailed(String message) {
        super(message);
    }

    public ImageDeleteFailed(String message, Throwable cause) {
        super(message, cause);
    }
}
