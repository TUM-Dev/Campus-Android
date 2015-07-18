package de.tum.in.tumcampus.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by enricogiga on 05/06/2015.
 * content included in a holder module. It can be anything, a video resource, a link, a file, a section header  ecc
 * the kind of object is described in the type but to download it we should append the token
 * when type is content then the  info is in the content attribute and file url is set to null;
 * TODO: manage the action to do when type= file  cause it can be pdf or html or other
 * used in the array of contents in a module
 */
public class MoodleCourseContent extends MoodleObject {
    /**
     * content's type
     * if it's file it's a resource to download if it's content than
     * the content attribute has text in html
     */
    private String type;
    /**
     * Name of the file
     */
    private String filename;
    /**
     * path of the file
     */
    private String filepath;
    /**
     * size of the file
     */
    private Number filesize;
    /**
     * content only if type = content
     */
    private String content;
    /**
     * time of creation
     */
    private String timecreated;
    /**
     * time of last edit
     */
    private String timemodified;
    /**
     * URL to the resource token has to be appended
     */
    private URL fileurl;
    /**
     * user id of the author?
     */
    private String userid;
    /**
     * author
     */
    private String author;
    /**
     * no idea
     */
    private String license;


    /**
     * Constructor to use if JSON is passed as a string
     *
     * @param jsonString JSON in string format
     */
    public MoodleCourseContent(String jsonString) {
        this(toJSONObject(jsonString));
    }

    /**
     * Constructor by parsing the JSONObject
     *
     * @param jsonObject the JSONObject
     */
    public MoodleCourseContent(JSONObject jsonObject) {
        this.type = null;
        this.filename = null;
        this.filesize = null;
        this.filepath = null;
        this.content = null;
        this.timecreated = null;
        this.timemodified = null;
        this.fileurl = null;
        this.userid = null;
        this.author = null;
        this.license = null;

        if (jsonObject != null) {
            this.type = jsonObject.optString("type");
            this.filename = jsonObject.optString("filename");
            this.filesize = jsonObject.optDouble("filesize");
            this.filepath = jsonObject.optString("filepath");
            this.content = jsonObject.optString("content");
            this.timecreated = jsonObject.optString("timecreated");
            this.timemodified = jsonObject.optString("timemodified");
            this.userid = jsonObject.optString("userid");
            this.author = jsonObject.optString("author");
            this.license = jsonObject.optString("license");

            try {
                this.fileurl = new URL(jsonObject.optString("fileurl"));
            } catch (MalformedURLException m) {
                this.fileurl = null;
                this.message = m.getMessage();
                this.exception = "MalformedURLException";
                this.errorCode = "malformedurl";
            }

        } else {
            this.exception = "EmptyJSONObjectException";
            this.message = "invalid json object passed to MoodleUserCourse model";
            this.errorCode = "emptyjsonobject";
            this.isValid = false;

        }

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Number getFilesize() {
        return filesize;
    }

    public void setFilesize(Number filesize) {
        this.filesize = filesize;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimecreated() {
        return timecreated;
    }

    public void setTimecreated(String timecreated) {
        this.timecreated = timecreated;
    }

    public String getTimemodified() {
        return timemodified;
    }

    public void setTimemodified(String timemodified) {
        this.timemodified = timemodified;
    }

    public URL getFileurl() {
        return fileurl;
    }

    public void setFileurl(URL fileurl) {
        this.fileurl = fileurl;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
