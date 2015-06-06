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
            }
        }
    }



    public MoodleToken(String token) {

        try {
            JSONObject tokenJSON = new JSONObject(token);
            new MoodleToken(tokenJSON);
            Utils.log("tokenstring is " + token);
        } catch (JSONException e) {
            this.token = null;
            this.message = e.getMessage();
            this.exception = "JSONException";
            this.errorCode = "invalidjson";
            e.printStackTrace();
        }
    }

}