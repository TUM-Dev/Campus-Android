package de.tum.in.tumcampus.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by enricogiga on 05/06/2015.
 * This class is for code reuse purpose. It holds attributes common to each moodle object retrieved from calls
 * to Moodle Web Services
 */
public class MoodleObject implements Serializable {

    protected String exception;
    protected String errorCode;
    protected String message;
    protected boolean isValid = true;
    private static final long serialVersionUID = -7060210544600464481L;

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

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * Created by enricogiga on 05/06/2015.
     * Helper method to convert a string to jsonobject.
     *
     * @param jsonString json in string format
     */
    protected static JSONObject toJSONObject(String jsonString) {
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.d("exception", e.getMessage());
            return null;
        }

    }
}
