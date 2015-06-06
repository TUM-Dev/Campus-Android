package de.tum.in.tumcampus.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;

/**
 * Created by enricogiga on 05/06/2015.
 *  A whole moodle course.
 *  It is just composed of a list of sections
 * Uses the core_course_get_contents function in the Moodle web service
 * this course is retrieved by sending the course id retrieved from the MoodleUserCourses
 * Todo:NOTICE only has a constructor from string because it can give error as an object
 */
public class MoodleCourse extends  MoodleObject{

    private List sections;


    /**
     *Used as a helper constructor if the json is passed as string
     */
    public MoodleCourse(String jsonstring)  {
        try{
        Object json = new JSONTokener(jsonstring).nextValue();
        if (json instanceof  JSONObject){
            //error
            JSONObject jsonObject = new JSONObject(jsonstring);
            this.sections = null;
            this.exception = jsonObject.optString("exception");
            this.errorCode = jsonObject.optString("errorcode");
            this.message = jsonObject.optString("message");
        } else {
            //good
            JSONArray jsonArray = new JSONArray(jsonstring);
            new MoodleCourse(jsonArray);

        }

        }
        catch (JSONException e){
            this.sections = null;

            this.message="invalid json parsing in MoodleCourse model";
            this.exception="JSONException";
            this.errorCode="invalidjson";
        }

    }

    /**
     *Constructor of the object from JSONObject
     * @param jsonArray = JSONArray to parse
     */
    public MoodleCourse(JSONArray jsonArray){
        this.sections=null;


        if (jsonArray != null){
            MoodleCourseSection section;
            try{
                for (int i=0; i < jsonArray.length(); i++){
                    JSONObject c = jsonArray.getJSONObject(i);
                    section = new MoodleCourseSection(c);
                    sections.add(section);
                }

            } catch (JSONException e){
                this.sections = null;

                this.message="invalid json parsing in MoodleCourse model";
                this.exception="JSONException";
                this.errorCode="invalidjson";
            }


        } else {
            this.exception = "EmptyJSONArrayException";
            this.message = "invalid json object passed to MoodleCourseSection model";
            this.errorCode = "emptyjsonobject";

        }

    }

    public List getSections() {
        return sections;
    }

    public void setSections(List sections) {
        this.sections = sections;
    }
}