package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.os.AsyncTask;

import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MoodleToken;
import de.tum.in.tumcampus.models.MoodleUser;

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
public class RealMoodleManager implements MoodleManager {

    final String SERVICE_BASE_URL = "http://school.demo.moodle.net";

    private MoodleToken moodleUserToken;

    private MoodleUser moodleUserInfo;
    /**
     * Getter for currentContext
     * @return currentContext
     */
    public Context getCurrentContext() {
        return currentContext;
    }

    /**
     * Setter for currentContext
     * @param currentContext
     */
    public void setCurrentContext(Context currentContext) {
        this.currentContext = currentContext;
    }

    private Context currentContext = null;

    /**
     * Getter for moodleUserToken
     * @return
     */
    public MoodleToken getMoodleUserToken() {
        return moodleUserToken;
    }

    /**
     * Setter for moodleUserToken
     * @param moodleUserToken
     */
    public void setMoodleUserToken(MoodleToken moodleUserToken) {
        this.moodleUserToken = moodleUserToken;
    }
    /**
     * Getter for MoodleUSerInfo
     * @return
     */
    public MoodleUser getMoodleUserInfo() {
        return moodleUserInfo;
    }

    /**
     * Setter for moodleUserInfo
     * @param moodleUserInfo
     */
    public void setMoodleUserInfo(MoodleUser moodleUserInfo) {
        this.moodleUserInfo = moodleUserInfo;
    }

    /**
     * This method starts the API call to Moodle's server that will get the user's access token
     * @param currentContext
     * @param username
     * @param password
     */
    public void requestUserToken(Context currentContext, String username, String password){
        String service = "//login/token.php?username=" + username + "&password=" + password +"&service=moodle_mobile_app";
        GenericMoodleRequestAsyncTask userTokenTask = new GenericMoodleRequestAsyncTask(new RequestUserTokenMoodleAPICommand() , currentContext, service);
        userTokenTask.execute();
    }
    public void requestUserData() {
        if (moodleUserToken != null && moodleUserToken.isValid()) {
            Utils.log("requesting user data...");
            String service = "/webservice/rest/server.php?wstoken=" + moodleUserToken.getToken() + "&wsfunction=core_webservice_get_site_info&moodlewsrestformat=json";
            GenericMoodleRequestAsyncTask userDataTask = new GenericMoodleRequestAsyncTask(new RequestUserInfoMoodleAPICommand() , currentContext, service);
            userDataTask.execute();
        }
        else Utils.log("error requesting user data...");
    }

    /**
     * AsyncTask that executes all Moodle's the API calls
     *
     * @param <Params>
     * @param <Progress>
     * @param <Result>
     */
    private class GenericMoodleRequestAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Object> {

        private MoodleApiCommand command;
        private Context context;
        private String service;
        private String jsonString;

        public GenericMoodleRequestAsyncTask(MoodleApiCommand command, Context context, String service) {
            super();
            this.command = command;
            this.context = context;
            this.service = service;
            setCurrentContext(context);
        }

        @Override
        protected Object doInBackground(Params... params) {
            String completeURL = SERVICE_BASE_URL + service;
            String testMoodleString = null;
            try {
                testMoodleString = NetUtils.downloadJson(context, completeURL).toString();
                return command.execute(testMoodleString);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

        }
    }

    /**
     * Interface for the command pattern. For each API call response a new Command is implemented
     */
    private interface MoodleApiCommand {
        public Object execute(String jsonString);
    }

    /**
     * Command that handles the creation of a new MoodelToken object
     */
    public final class RequestUserTokenMoodleAPICommand implements MoodleApiCommand {
        @Override
        public Object execute(String jsonString) {

            MoodleToken moodleToken = new MoodleToken(jsonString);

            if (moodleToken.isValid()){
                setMoodleUserToken(moodleToken);
                Utils.log("UserToken is valid");
                requestUserData();
               //TODO: add method to save token for future connections
            }else {
               setMoodleUserToken(null);
            }

            return moodleToken;
        }
    }
    /**
     * Command that handles the creation of a new MoodleUser object
     */
    public final class RequestUserInfoMoodleAPICommand implements MoodleApiCommand {
        @Override
        public Object execute(String jsonString) {

            MoodleUser moodleUser = new MoodleUser(jsonString);

            if (moodleUser.isValid()){
                setMoodleUserInfo(moodleUser);
                Utils.log("UserInfo is valid");
                //TODO add method to cache user info
            }else {
                setMoodleUserInfo(null);
            }

            return moodleUser;

        }
    }


}
