package de.tum.in.tumcampus.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by enricogiga on 05/06/2015.
 * Object holding the information on a Moodle course list element
 * Uses the core_enrol_get_users_courses function in the Moodle web service
 * the complete course is retrieved by sending the course id retrieved from this call

 *
 * TODO: summary may be in html, we either display it or have to parse it. I have a link to showing a html content in android
 * summaryformat=1 for html
 *
 * didn't understand what format is
 *
 */


public class MoodleUserCourses extends  MoodleObject{

    private Number enrolledusercount;
    private String format;
    private String fullname;
    private Number id;
    private String lang;
    private String shortname;
    private boolean showgrades;
    private String summary;
    private Number summaryformat;
    private Number visible;


    /**
     *Constructor of the object from JSONObject
     * @param jsonObject = JSONObject to parse
     */
    public MoodleUserCourses(JSONObject jsonObject){
        this.enrolledusercount=null;
        this.format=null;
        this.fullname=null;
        this.id=null;
        this.lang=null;
        this.shortname=null;
        this.showgrades=false;
        this.summary=null;
        this.summaryformat=null;
        this.visible=null;
        if (jsonObject != null){

                this.enrolledusercount = jsonObject.optInt("enrolledusercount");
                this.format = jsonObject.optString("format");
                this.fullname = jsonObject.optString("fullname");
                this.id = jsonObject.optInt("id");
                this.lang = jsonObject.optString("lang");
                this.shortname = jsonObject.optString("shortname");
                this.showgrades = jsonObject.optBoolean("showgrades");
                this.summary = jsonObject.optString("summary");
                this.summaryformat = jsonObject.optDouble("summaryformat");
                this.visible = jsonObject.optDouble("visible");


        } else {
            this.exception = "EmptyJSONObjectException";
            this.message = "invalid json object passed to MoodleUserCourses model";
            this.errorCode = "emptyjsonobject";
            this.isValid=false;

        }

    }


    public Number getEnrolledusercount(){
        return this.enrolledusercount;
    }
    public void setEnrolledusercount(Number enrolledusercount){
        this.enrolledusercount = enrolledusercount;
    }
    public String getFormat(){
        return this.format;
    }
    public void setFormat(String format){
        this.format = format;
    }
    public String getFullname(){
        return this.fullname;
    }
    public void setFullname(String fullname){
        this.fullname = fullname;
    }
    public Number getId(){
        return this.id;
    }
    public void setId(Number id){
        this.id = id;
    }
    public String getLang(){
        return this.lang;
    }
    public void setLang(String lang){
        this.lang = lang;
    }
    public String getShortname(){
        return this.shortname;
    }
    public void setShortname(String shortname){
        this.shortname = shortname;
    }
    public boolean getShowgrades(){
        return this.showgrades;
    }
    public void setShowgrades(boolean showgrades){
        this.showgrades = showgrades;
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
