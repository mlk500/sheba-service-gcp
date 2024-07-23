package sheba.backend.app.exceptions;
public class MediaUploadFailed extends RuntimeException {
    public MediaUploadFailed(String message) {
        super(message);
    }

    public MediaUploadFailed(String message, Throwable cause) {
        super(message, cause);
    }
}
