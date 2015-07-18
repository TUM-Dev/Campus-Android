package de.tum.in.tumcampus.models;

import org.json.JSONObject;

/**
 * Created by enricogiga on 05/06/2015.
 * Token for moodle access. The token will have to be used in every request to the Moodle web service and to the Moodle file service while downloading
 * <p/>
 * Might need to be refreshed
 */
public class MoodleToken extends MoodleObject {

    /**
     * token to be used in the Moodle webService calls
     */
    private String token;

    /**
     * Constructor by parsing the JSONObject
     *
     * @param jsonObject the JSONObject
     */
    public MoodleToken(JSONObject jsonObject) {
        this.token = "";
        if (jsonObject != null) {
            if (!jsonObject.has("error")) {

                token = jsonObject.optString("token");
                this.isValid = true;
            } else {
                this.isValid = false;
                this.exception = "WrongCredentialsException";
                this.errorCode = "wrongcredentials";
                this.message = jsonObject.optString("error");
            }
        } else {
            this.isValid = false;
            this.exception = "JSONException";
            this.errorCode = "invalidjsonstring";
            this.message = "error while parsing jsonstring in " + this.getClass().getName();
        }
    }

    /**
     * Constructor to use if JSON is passed as a string
     *
     * @param jsonString JSON in string format
     */
    public MoodleToken(String jsonString) {
        this(toJSONObject(jsonString));
    }

    public MoodleToken() {
        this.token = null;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}