package de.tum.in.tumcampus.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;

/**
 * Created by enricogiga on 06/06/2015.
 */
public class MoodleUserCoursesList extends  MoodleObject{

    private List userCourses;


    /**
     *Used as a helper constructor if the json is passed as string. if the jsonarray
     * is passed it should be checked outside weather it's an array (OK) or object (ERROR)
     */
    public MoodleUserCoursesList(String jsonstring)  {
        try{
            Object json = new JSONTokener(jsonstring).nextValue();
            if (json instanceof  JSONObject){
                //error
                JSONObject jsonObject = new JSONObject(jsonstring);
                this.userCourses = null;
                this.exception = jsonObject.optString("exception");
                this.errorCode = jsonObject.optString("errorcode");
                this.message = jsonObject.optString("message");
                this.isValid=false;
            } else {
                //good
                JSONArray jsonArray = new JSONArray(jsonstring);
                this.userCourses=null;


                if (jsonArray != null){
                    MoodleUserCourses userCourse;
                    try{
                        for (int i=0; i < jsonArray.length(); i++){
                            JSONObject c = jsonArray.getJSONObject(i);
                            userCourse = new MoodleUserCourses(c);
                            userCourses.add(userCourse);
                        }

                    } catch (JSONException e){
                        this.userCourses = null;

                        this.message="invalid json parsing in MoodleCourse model";
                        this.exception="JSONException";
                        this.errorCode="invalidjson";
                        this.isValid=false;
                    }


                } else {
                    this.exception = "EmptyJSONArrayException";
                    this.message = "invalid json object passed to MoodleCourseSection model";
                    this.errorCode = "emptyjsonobject";
                    this.isValid=false;

                }

            }

        }
        catch (JSONException e){
            this.userCourses = null;

            this.message="invalid json parsing in MoodleCourse model";
            this.exception="JSONException";
            this.errorCode="invalidjson";
            this.isValid=false;
        }

    }

    /**
     *Constructor of the object from JSONObject
     * @panram jsonArray = JSONArray to parse
     */
  //  public MoodleUserCoursesList(JSONArray jsonArray){


   // }

    public List getSections() {
        return userCourses;
    }

    public void setSections(List sections) {
        this.userCourses = sections;
    }

}
