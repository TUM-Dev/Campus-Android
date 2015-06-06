package de.tum.in.tumcampus.models.managers;

import android.content.Context;

import org.json.JSONObject;

import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Handles all calls done with the MoodleAPIs
 *
 * NOTA BENE: since the APIs aren't activated on the TUM's Moodle platform
 *            for this moment the calls will be made on the example application
 *            available in Moodle's website.
 *
 *            Orange school DEMO
 *            link:"http://school.demo.moodle.net/"
 *
 *            private static final String SERVICE_BASE_URL = "http://school.demo.moodle.net//login/token.php?";
 */
public class MoodleManager {

    private String moodleUserToken;
    private boolean isFetching;

    /**
     * This method handles the calls made to the MoodleAPI
     * Input:
     *      serviceAddress  -> the address of the service that is being called
     *      context -> current context
     */
    public void getJSON(final String serviceAddress, Context context){
        //Test for moodleAPIs
        final Context currentContext = context;
        final String SERVICE_BASE_URL = "http://school.demo.moodle.net//login/token.php?";

        Thread backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isFetching = true;
                Utils.log(" Hello Thread!");
                try {
                    JSONObject testMoodleJson = NetUtils.downloadJson(currentContext, SERVICE_BASE_URL + serviceAddress);

                    Utils.log("JSON Object Moodle is: " + testMoodleJson.toString());
                    setAccessToken(testMoodleJson);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isFetching = false;
            }

        });
        backgroundThread.start();
    }

    /**
     * This function creates the Token for the moodle login
     *
     * @param jsonObject containing the responce of the MoodleAPI call
     */
    public void setAccessToken(JSONObject jsonObject){
        String token = "";
        if (jsonObject.has("token")){
            token = jsonObject.optString("token");
            Utils.log("token is " + token);
        }
        else {
            Utils.log("Error getting token");
        }
        setMoodleUserToken(token.equals("") ? null : token);
    }

    /**
     * This function checks whether or not the HTTP request made with the Moodle APIs is completed or not
     * @return isFetching -> bool value for the Request progress status
     */
    public boolean checkRequestIsDone(){
        if (isFetching == false){
            Utils.log("JSON Request in progress");
        }
        return isFetching;
    }

    /**
     * Function performing the request for the user autentication
     *
     * @param username user's username
     * @param password user's password
     * @param context current context
     */
    public void requestUserToken (String username, String password, Context context){
        final String requestURL = "username=" + username + "&password=" + password + "&service=moodle_mobile_app";
        getJSON(requestURL, context);
    }
    /**
     * Getter for moodleUserToken
     * @return token
     */
    public String getMoodleUserToken() {
        return checkRequestIsDone() ? null : moodleUserToken;
    }

    /**
     * Setter for moodleUserToken
     * @param moodleUserToken
     */
    public void setMoodleUserToken(String moodleUserToken) {
        this.moodleUserToken = moodleUserToken;
    }
}
