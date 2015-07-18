package de.tum.in.tumcampus.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Created by enricogiga on 05/06/2015.
 * A whole moodle course.
 * It is just composed of a list of sections
 * Uses the core_course_get_contents function in the Moodle web service
 * this course is retrieved by sending the course id retrieved from the MoodleUserCourse
 */
public class MoodleCourse extends  MoodleObject{

    /**
     * List of sections of which a course is composed
     * section is a name given by me, the actual json array has no name
     */
    private List<MoodleCourseSection> sections;


    /**
     *Constructor from json string since unfortunately
     * if an error occurs a JSONObject is retrieved describing the error
     * else a JSONArray with the right contents is retrieved
     *
     * @param jsonstring JSON in string format
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
                this.isValid=false;
            } else {
                //good
                JSONArray jsonArray = new JSONArray(jsonstring);
                this.sections=new ArrayList<>();


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
                    this.isValid=false;
                }

            }

        }
        catch (JSONException e){
            this.sections = null;

            this.message="invalid json parsing in MoodleCourse model";
            this.exception="JSONException";
            this.errorCode="invalidjson";
            this.isValid=false;
        }

    }



    public List<MoodleCourseSection> getSections() {
        return sections;
    }

    public void setSections(List<MoodleCourseSection> sections) {
        this.sections = sections;
    }

    public List<String> getSectionNames(){
        List<String> sectionNames = new ArrayList<>();
        if (sections != null) {
            for (MoodleCourseSection s : sections)
                sectionNames.add((s).getName());
            return sectionNames;
        }
        else
            Utils.log("sections is null !");
        return null;
    }
}