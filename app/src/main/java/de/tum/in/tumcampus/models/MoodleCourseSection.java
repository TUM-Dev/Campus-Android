package de.tum.in.tumcampus.models;

/**
 * Created by enricogiga on 05/06/2015.
 Object holding the information on a section of a course (components of the course main page on moodle)
 * Uses the core_course_get_contents function in the Moodle web service
 * this course section is retrieved by sending the course id retrieved from the MoodleUserCourses
 */
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MoodleCourseSection extends  MoodleObject{
    private Number id;
    private List modules;
    private String name;
    private String summary;
    private Number summaryformat;
    private Number visible;

    /**
     *Used as a helper constructor if the json is passed as string
     */
    public MoodleCourseSection(String jsonstring)  {
        try{
            JSONObject obj = new JSONObject(jsonstring);
            new MoodleCourseSection(obj);
        }
        catch (JSONException e){
            this.id=null;
            this.modules=null;
            this.name=null;
            this.summary=null;
            this.visible=null;
            this.summaryformat=null;

            this.message="invalid json parsing in MoodleCourseSection model";
            this.exception="JSONException";
            this.errorCode="invalidjson";
        }

    }

    /**
     *Constructor of the object from JSONObject
     * @param jsonObject = JSONObject to parse
     */
    public MoodleCourseSection(JSONObject jsonObject){
        this.id=null;
        this.modules=null;
        this.name=null;
        this.summary=null;
        this.visible=null;
        this.summaryformat=null;
        if (jsonObject != null){
            if (!jsonObject.has("exception")) {


                MoodleCourseModule module;
                try{
                    JSONArray jsonArray = jsonObject.getJSONArray("modules");
                    for (int i=0; i < jsonArray.length(); i++){
                        JSONObject c = jsonArray.getJSONObject(i);
                        module = new MoodleCourseModule(c);
                        modules.add(module);
                    }

                } catch (JSONException e){
                    this.modules = null;

                    this.message="invalid json parsing in MoodleCourseSection model";
                    this.exception="JSONException";
                    this.errorCode="invalidjson";
                }

                this.id = jsonObject.optInt("id");
                this.name = jsonObject.optString("format");
                this.summary = jsonObject.optString("summary");
                this.summaryformat = jsonObject.optDouble("summaryformat");
                this.visible = jsonObject.optDouble("visible");
            } else{
                this.exception = jsonObject.optString("exception");
                this.errorCode = jsonObject.optString("errorcode");
                this.message = jsonObject.optString("message");

            }

        } else {
            this.exception = "EmptyJSONObjectException";
            this.message = "invalid json object passed to MoodleCourseSection model";
            this.errorCode = "emptyjsonobject";

        }

    }

    public Number getId(){
        return this.id;
    }
    public void setId(Number id){
        this.id = id;
    }
    public List getModules(){
        return this.modules;
    }
    public void setModules(List modules){
        this.modules = modules;
    }
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getSummary(){
        return this.summary;
    }
    public void setSummary(String summary){
        this.summary = summary;
    }
    public Number getSummaryformat(){
        return this.summaryformat;
    }
    public void setSummaryformat(Number summaryformat){
        this.summaryformat = summaryformat;
    }
    public Number getVisible(){
        return this.visible;
    }
    public void setVisible(Number visible){
        this.visible = visible;
    }
}
