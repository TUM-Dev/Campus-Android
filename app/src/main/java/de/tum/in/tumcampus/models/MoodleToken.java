package de.tum.in.tumcampus.models;

import org.json.JSONException;
import org.json.JSONObject;

import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Created by enricogiga on 05/06/2015.
 * Token for moodle access. The token will have to be used in every request to the Moodle web service and to the Moodle file service while downloading
 *
 * Might need to be refreshed
 *
 */
public class MoodleToken extends MoodleObject {

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String token;


    public MoodleToken(JSONObject jsonObject) {
        this.token = "";
        if (jsonObject != null) {
            if (!jsonObject.has("error")) {

                token = jsonObject.optString("token");
                this.isValid=true;
            }else {
                this.isValid=false;
                this.exception = "WrongCredentialsException";
                this.errorCode = "wrongcredentials";
                this.message = jsonObject.optString("error");



            }

        } else {
            this.isValid=false;
            this.exception= "JSONException";
            this.errorCode= "invalidjsonstring";
            this.message = "error while parsing jsonstring in "+this.getClass().getName();
        }

    }



    public MoodleToken(String jsonString) {
        this(toJSONObject(jsonString));

    }

}