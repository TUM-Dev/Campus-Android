package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.MoodleLoginActivity;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MoodleCourse;
import de.tum.in.tumcampus.models.MoodleEvent;
import de.tum.in.tumcampus.models.MoodleEventsList;
import de.tum.in.tumcampus.models.MoodleToken;
import de.tum.in.tumcampus.models.MoodleUser;
import de.tum.in.tumcampus.models.MoodleUserCourse;
import de.tum.in.tumcampus.models.MoodleUserCourseList;

/**
 * Handles all calls done with the MoodleAPIs
 * <p/>
 * NOTA BENE: since the APIs aren't activated on the TUM's Moodle platform
 * for this moment the calls will be made on the example application
 * available in Moodle's website.
 * <p/>
 * Orange school DEMO
 * link:"http://school.demo.moodle.net/"
 * <p/>
 * private static final String SERVICE_BASE_URL = "http://school.demo.moodle.net//login/token.php?";
 */
public class RealMoodleManager extends MoodleManager {

    final String SERVICE_BASE_URL = "http://school.demo.moodle.net";
    //final String SERVICE_BASE_URL = "https://dev.moodle.tum.de";
    Context currentContext;
    private static RealMoodleManager instance;

    /**
     * Getting or creating singleton
     *
     * @param delegate context
     * @return the manager instace
     */
    public static RealMoodleManager getInstance(MoodleUpdateDelegate delegate, Context context) {
        if (instance == null) {
            instance = new RealMoodleManager(delegate);
        } else {
            instance.setDelegate(delegate);
        }
        instance.setCurrentContext(context);
        return instance;
    }

    private RealMoodleManager(MoodleUpdateDelegate delegate) {

        super(delegate);
    }


    /**
     * This method starts the API call to Moodle's server that will get the user's access token
     *
     * @param currentContext the current context
     * @param username       the moodle username
     * @param password       the corresponding password
     */
    public void requestUserToken(Context currentContext, String username, String password) {
        //removeToken();
        //if (!loadUserToken()) {
        String service = "//login/token.php?username=" + username + "&password=" + password + "&service=moodle_mobile_app";
        GenericMoodleRequestAsyncTask userTokenTask = new GenericMoodleRequestAsyncTask(new RequestUserTokenMoodleAPICommand(), currentContext, service);
        userTokenTask.execute();
        //}
        //else {
        //    requestUserData(currentContext);
        //}
    }

    /**
     * This method starts the API call to Moodle's server that will get the user's info data
     */
    public void requestUserData(Context currentContext) {
        if (this.getMoodleUserToken() != null && this.getMoodleUserToken().isValid()) {
            Utils.log("requesting user data...");
            String service = "/webservice/rest/server.php?wstoken=" + this.getMoodleUserToken().getToken() + "&wsfunction=core_webservice_get_site_info&moodlewsrestformat=json";
            GenericMoodleRequestAsyncTask userDataTask = new GenericMoodleRequestAsyncTask(new RequestUserInfoMoodleAPICommand(), currentContext, service);
            userDataTask.execute();
        } else Utils.log("error requesting user data...");
    }

    /**
     * This method starts the API call to Moodle's server that will get the user's course list
     */
    public void requestUserCourseList(Context currentContext) {
        if (this.getMoodleUserToken() != null && this.getMoodleUserToken().isValid()) {
            Utils.log("requesting user course list...");
            String service = "/webservice/rest/server.php?wstoken=" + this.getMoodleUserToken().getToken() + "&wsfunction=core_enrol_get_users_courses&moodlewsrestformat=json&userid=" + getMoodleUserInfo().getUserid();

            GenericMoodleRequestAsyncTask userDataTask = new GenericMoodleRequestAsyncTask(new RequestUserCoursesMoodleAPICommand(), currentContext, service);
            userDataTask.execute();
        } else Utils.log("error requesting user course list...");

    }

    /**
     * This method starts the API call to Moodle's server that will get the user's course info
     */
    public void requestUserCourseInfo(Context currentContext, int courseId) {
        if (this.getMoodleUserToken() != null && this.getMoodleUserToken().isValid()) {
            Utils.log("requesting user course info...");
            String service = "/webservice/rest/server.php?wstoken=" + this.getMoodleUserToken().getToken() + "&wsfunction=core_course_get_contents&moodlewsrestformat=json&courseid=" + courseId;

            GenericMoodleRequestAsyncTask userDataTask = new GenericMoodleRequestAsyncTask(new RequestUserCourseInfoMoodleAPICommand(), currentContext, service);
            userDataTask.execute();
        } else Utils.log("error requesting user course info...");

    }

    /**
     * This method starts the API call to Moodle's server that will get the user's course info
     */
    public void requestUserEvents(Context currentContext) {
        if (this.getMoodleUserToken() != null && this.getMoodleUserToken().isValid()) {
            Utils.log("requesting user's events list");
            String service = "/webservice/rest/server.php?wstoken=" + getMoodleUserToken().getToken() + "&wsfunction=Core_calendar_get_calendar_events&moodlewsrestformat=json";

            GenericMoodleRequestAsyncTask userDataTask = new GenericMoodleRequestAsyncTask(new RequestUserEventsAPICommand(), currentContext, service);
            userDataTask.execute();
        } else Utils.log("error requesting user's event list...");

    }


    /**
     * AsyncTask that executes all Moodle's the API calls
     *
     * @param <Params>
     * @param <Progress>
     * @param <Result>
     */
    private class GenericMoodleRequestAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Object> {

        private MoodleAPICommand command;
        private Context context;
        private String service;

        public GenericMoodleRequestAsyncTask(MoodleAPICommand command, Context context, String service) {
            super();
            this.command = command;
            this.context = context;
            this.service = service;
            setCurrentContext(context);
        }

        @Override
        protected Object doInBackground(Params... params) {
            String completeURL = SERVICE_BASE_URL + service;
            String resultAPIcallJSONString;
            try {

                resultAPIcallJSONString = NetUtils.downloadStringHttp(completeURL, context);
                command.execute(resultAPIcallJSONString);
                return command;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (command.isUpdating()) {
                Utils.log("calling refresh for command " + command.toString());
                if (getDelegate() != null)
                    getDelegate().refresh();
            }
        }
    }

    /**
     * Interface for the command pattern. For each API call response a new Command is implemented
     */
    private abstract class MoodleAPICommand {
        private boolean shouldUpdate;

        public boolean isUpdating() {
            return shouldUpdate;
        }

        public void setShouldUpdate(boolean shouldUpdate) {
            this.shouldUpdate = shouldUpdate;
        }


        public abstract Object execute(String jsonString);
    }

    /**
     * Command that handles the creation of a new MoodelToken object
     */
    public final class RequestUserTokenMoodleAPICommand extends MoodleAPICommand {
        public RequestUserTokenMoodleAPICommand() {
            setShouldUpdate(true);
        }

        @Override
        public Object execute(String jsonString) {

            MoodleToken moodleToken = new MoodleToken(jsonString);

            if (moodleToken.isValid()) {
                setMoodleUserToken(moodleToken);
                Utils.log("UserToken is valid");

                saveUserToken();
            } else {
                if (moodleToken.getErrorCode().equals("invalidtoken")) {
                    Utils.log("invalid token!");
                    createLoginActivity();
                }
                setMoodleUserToken(null);
                //removeToken();
            }

            return moodleToken;
        }
    }

    /**
     * Command that handles the creation of a new MoodleUser object
     */
    public final class RequestUserInfoMoodleAPICommand extends MoodleAPICommand {
        public RequestUserInfoMoodleAPICommand() {
            setShouldUpdate(true);
        }

        @Override
        public Object execute(String jsonString) {

            MoodleUser moodleUser = new MoodleUser(jsonString);

            if (moodleUser.isValid()) {
                setMoodleUserInfo(moodleUser);
                Utils.log("UserInfo is valid");
                //TODO add method to cache user info
            } else {
                Utils.log("Warning ! UserInfo is not valid!");
                setMoodleUserInfo(null);
                //TODO check if error is for invalid token and delete cached version of token
                createLoginActivity();
                if (moodleUser.getMessage().contains("token")) {
                    createLoginActivity();
                }

                Toast.makeText(currentContext, "Login session has expired", Toast.LENGTH_LONG).show();
                removeToken();
            }

            return moodleUser;

        }
    }

    /**
     * Command that handles the creation of a new MoodleCoursesList object
     */
    public final class RequestUserCoursesMoodleAPICommand extends MoodleAPICommand {
        public RequestUserCoursesMoodleAPICommand() {
            setShouldUpdate(true);
        }

        @Override
        public Object execute(String jsonString) {

            MoodleUserCourseList moodleUserCourseList = new MoodleUserCourseList(jsonString);
            if (moodleUserCourseList.isValid()) {
                setMoodleUserCourseList(moodleUserCourseList);
                Utils.log("UserCoursesList is valid");
            } else {
                setMoodleUserCourseList(null);
                //TODO check if error is for invalid token and delete cached version of token
                if (moodleUserCourseList.getMessage().contains("token")) {
                    createLoginActivity();
                }
                Toast.makeText(currentContext, "Login session has expired", Toast.LENGTH_LONG).show();
                removeToken();
            }

            return moodleUserCourseList;

        }
    }

    /**
     * Command that handles the creation of a new MoodleCourse object
     */
    public final class RequestUserCourseInfoMoodleAPICommand extends MoodleAPICommand {
        public RequestUserCourseInfoMoodleAPICommand() {
            setShouldUpdate(true);
        }

        @Override
        public Object execute(String jsonString) {

            MoodleCourse moodleCourse = new MoodleCourse(jsonString);
            if (moodleCourse.isValid()) {
                setMoodleUserCourseInfo(moodleCourse);
                Utils.log("UserCourseInfo is valid");
            } else {
                setMoodleUserCourseInfo(null);
                //TODO check if error is for invalid token and delete cached version of token
                if (moodleCourse.getMessage().contains("token")) {
                    createLoginActivity();
                }
                Toast.makeText(currentContext, "Login session has expired", Toast.LENGTH_LONG).show();
                removeToken();
            }

            return moodleCourse;

        }
    }

    /**
     * Command that handles the creation of a new MoodleEvents List
     */
    public final class RequestUserEventsAPICommand extends MoodleAPICommand {
        public RequestUserEventsAPICommand() {
            setShouldUpdate(true);
        }

        @Override
        public Object execute(String jsonString) {

            MoodleEventsList eventsList;
            eventsList = new MoodleEventsList(jsonString);
            if (eventsList.isValid()) {
                setMoodleUserEventsList(eventsList);
                Utils.log("MoodleUserEventsList is valid");
            } else {
                setMoodleUserEventsList(null);
                //TODO check if error is for invalid token and delete cached version of token
                if (eventsList.getMessage().contains("token")) {
                    createLoginActivity();
                }
                Toast.makeText(currentContext, "Login session has expired", Toast.LENGTH_LONG).show();
                removeToken();
            }

            return eventsList;

        }
    }

    /**
     * Getters and Setters Methods
     */
    @Override
    public ArrayList<MoodleEvent> getUserEvents() {

        return getMoodleUserEventsList().getSections();
    }

    /**
     * @return map of course names to course Ids
     */
    @Override
    public Map<String, String> getCoursesList() {
        if (getMoodleUserCourseList() == null) return null;
        Map<String, String> userCoursesMap = new HashMap<>();

        for (Object courseObj : getMoodleUserCourseList().getSections()) {
            MoodleUserCourse course = (MoodleUserCourse) courseObj;

            userCoursesMap.put(course.getFullname(), course.getSummary());
        }

        return userCoursesMap;
    }

    @Override
    public Map<String, Integer> getCoursesId() {
        if (getMoodleUserCourseList() == null) return null;
        Map<String, Integer> userCoursesMap = new HashMap<>();

        for (Object courseObj : getMoodleUserCourseList().getSections()) {
            MoodleUserCourse course = (MoodleUserCourse) courseObj;

            userCoursesMap.put(course.getFullname(), (int) course.getId());
        }

        return userCoursesMap;
    }

    public Context getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(Context currentContext) {
        this.currentContext = currentContext;
    }

    public MoodleToken getMoodleUserToken() {
        return moodleUserToken;
    }

    public void setMoodleUserToken(MoodleToken moodleUserToken) {
        this.moodleUserToken = moodleUserToken;
    }

    public MoodleUser getMoodleUserInfo() {
        return moodleUserInfo;
    }

    public void setMoodleUserInfo(MoodleUser moodleUserInfo) {
        this.moodleUserInfo = moodleUserInfo;
    }

    public MoodleUserCourseList getMoodleUserCourseList() {
        return moodleUserCourseList;
    }

    public void setMoodleUserCourseList(MoodleUserCourseList moodleUserCourseList) {
        this.moodleUserCourseList = moodleUserCourseList;
    }

    public MoodleCourse getMoodleUserCourseInfo() {
        return moodleUserCourseInfo;
    }

    public void setMoodleUserCourseInfo(MoodleCourse moodleUserCourseInfo) {
        this.moodleUserCourseInfo = moodleUserCourseInfo;
    }

    public MoodleEventsList getMoodleUserEventsList() {
        return moodleUserEventsList;
    }

    public void setMoodleUserEventsList(MoodleEventsList moodleUserEventsList) {
        this.moodleUserEventsList = moodleUserEventsList;
    }

    public String getToken() {
        try {
            return getMoodleUserToken().getToken();
        } catch (Exception e) {
            Utils.log(e);
            return null;
        }
    }

    /**
     * This method saves in shared preferences the user token avoiding making a
     * login request each time
     */
    public void saveUserToken() {
        if (currentContext != null) {
            SharedPreferences sharedPreferences = currentContext.getSharedPreferences(currentContext.getResources().getString(R.string.moodle_user_shared_prefs_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(currentContext.getResources().getString(R.string.moodle_token_key), getToken());
            editor.apply();
        }
    }

    /**
     * this method loads the cached version of the token
     */
    @Override
    public boolean loadUserToken() {
        SharedPreferences sharedPreferences = currentContext.getSharedPreferences(currentContext.getResources().getString(R.string.moodle_user_shared_prefs_key), Context.MODE_PRIVATE);
        String token = sharedPreferences.getString((currentContext.getResources().getString(R.string.moodle_token_key)), null);

        if (token != null) {
            MoodleToken moodleToken = new MoodleToken();
            moodleToken.setToken(token);
            setMoodleUserToken(moodleToken);
            Utils.log("Load cached moodle token");
            Utils.log("Moodle token is: " + token);
            return true;
        }

        Utils.log("Faild to load cached moodle token");
        return false;
    }

    /**
     * This method removes the cached version of the moodle token
     */
    public void removeToken() {
        SharedPreferences sharedPreferences = currentContext.getSharedPreferences(currentContext.getResources().getString(R.string.moodle_user_shared_prefs_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(currentContext.getResources().getString(R.string.moodle_token_key));
        editor.apply();

    }

    /**
     * This method creates a Login Activity whenever a token is expired
     */
    private void createLoginActivity() {

        Intent intent = new Intent(getCurrentContext(), MoodleLoginActivity.class);
        intent.putExtra("class", currentContext.getClass());
        intent.putExtra("outside_activity", true);
        getCurrentContext().startActivity(intent);

    }


}
