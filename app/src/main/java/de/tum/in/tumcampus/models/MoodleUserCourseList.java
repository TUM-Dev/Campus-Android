package de.tum.in.tumcampus.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enricogiga on 06/06/2015.
 * List holding MoodleUserCourse
 * Needed to address directly the result of the call to moodle's api function
 * Uses the core_enrol_get_users_courses function in the Moodle web service
 */
public class MoodleUserCourseList extends MoodleObject {

    /**
     * The list of User Courses
     */
    private List<MoodleUserCourse> userCourses = new ArrayList<>();


    /**
     * Constructor from json string since unfortunately
     * if an error occurs a JSONObject is retrieved describing the error
     * else a JSONArray with the right contents is retrieved
     *
     * @param jsonstring JSON in string format
     */
    public MoodleUserCourseList(String jsonstring) {
        try {
            Object json = new JSONTokener(jsonstring).nextValue();
            if (json instanceof JSONObject) {
                //error
                JSONObject jsonObject = new JSONObject(jsonstring);
                this.userCourses = null;
                this.exception = jsonObject.optString("exception");
                this.errorCode = jsonObject.optString("errorcode");
                this.message = jsonObject.optString("message");
                this.isValid = false;
            } else {
                //good
                JSONArray jsonArray = new JSONArray(jsonstring);


                MoodleUserCourse userCourse;
                try {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);
                        userCourse = new MoodleUserCourse(c);

                        userCourses.add(userCourse);
                    }

                } catch (JSONException e) {
                    this.userCourses = null;

                    this.message = "invalid json parsing in MoodleCourse model";
                    this.exception = "JSONException";
                    this.errorCode = "invalidjson";
                    this.isValid = false;
                }


            }

        } catch (JSONException e) {
            this.userCourses = null;

            this.message = "invalid json parsing in MoodleCourse model";
            this.exception = "JSONException";
            this.errorCode = "invalidjson";
            this.isValid = false;
        }

    }


    public List getSections() {
        return userCourses;
    }

    public void setSections(List<MoodleUserCourse> sections) {
        this.userCourses = sections;
    }

}
