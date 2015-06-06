package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.os.AsyncTask;

import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.models.MoodleToken;

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
    final String SERVICE_BASE_URL = "http://school.demo.moodle.net//login/token.php?";

    private MoodleToken moodleUserToken;

    private boolean isFetching;


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
     * This method starts the API call to Moodle's server that will get the user's access token
     * @param currentContext
     * @param service
     */
    public void requestServiceCall(Context currentContext, String service){
        GenericMoodleRequestAsyncTask userTokenTask = new GenericMoodleRequestAsyncTask(new RequestUserTokenMoodleApiCommand() , currentContext, service);
        userTokenTask.execute();
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
    public final class RequestUserTokenMoodleApiCommand implements MoodleApiCommand {
        @Override
        public Object execute(String jsonString) {
            MoodleToken moodleToken = new MoodleToken(jsonString);
            return moodleToken;
        }
    }


}
