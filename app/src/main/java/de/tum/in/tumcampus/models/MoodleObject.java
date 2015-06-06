package de.tum.in.tumcampus.models;

/**
 * Created by enricogiga on 05/06/2015.
 * This class is for code reuse purpose. It holds attributes common to each moodle object retrieved from calls
 * to Moodle Web Services
 */
public class MoodleObject {
    protected String exception;

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    protected String errorCode;
    protected String message;
}
