package de.tum.in.tumcampus.models;

import android.text.Html;

import org.json.JSONObject;

/**
 * Created by enricogiga on 05/06/2015.
 * Object holding the information on a Moodle course list element
 * Synthesized info on a user's course to display in a list
 * the complete course is retrieved by sending the course id retrieved from this call
 */
public class MoodleUserCourse extends MoodleObject {

    /**
     * Number of course partecipants
     */
    private Number enrolledusercount;
    /**
     * no idea
     */
    private String format;
    /**
     * course full name
     */
    private String fullname;
    /**
     * course id
     */
    private Number id;
    /**
     * course languange
     */
    private String lang;
    /**
     * course shortname
     */
    private String shortname;
    /**
     * course boolean for showing grades or no
     */
    private boolean showgrades;
    /**
     * course summary will be stripped of html if summaryformat = 1
     * needs to be called with to string if used with methods not accepting CharSequences
     */
    private String summary;
    /**
     * format of summary
     */
    private Number summaryformat;
    /**
     * course no idea
     */
    private Number visible;


    /**
     * Constructor to use if JSON is passed as a string
     *
     * @param jsonString JSON in string format
     */
    public MoodleUserCourse(String jsonString) {
        this(toJSONObject(jsonString));

    }

    /**
     * Constructor by parsing the JSONObject
     *
     * @param jsonObject the JSONObject
     */
    public MoodleUserCourse(JSONObject jsonObject) {
        this.enrolledusercount = null;
        this.format = null;
        this.fullname = null;
        this.id = null;
        this.lang = null;
        this.shortname = null;
        this.showgrades = false;
        this.summary = null;
        this.summaryformat = null;
        this.visible = null;
        if (jsonObject != null) {

            this.enrolledusercount = jsonObject.optInt("enrolledusercount");
            this.format = jsonObject.optString("format");
            this.fullname = jsonObject.optString("fullname");
            this.id = jsonObject.optInt("id");
            this.lang = jsonObject.optString("lang");
            this.shortname = jsonObject.optString("shortname");
            this.showgrades = jsonObject.optBoolean("showgrades");
            this.summaryformat = jsonObject.optDouble("summaryformat");
            this.summary = jsonObject.optString("summary");
            if (summaryformat.doubleValue() == 1) {
                this.summary = stripHtml(summary);
            }
            this.visible = jsonObject.optDouble("visible");
        } else {
            this.exception = "EmptyJSONObjectException";
            this.message = "invalid json object passed to MoodleUserCourse model";
            this.errorCode = "emptyjsonobject";
            this.isValid = false;
        }
    }

    /**
     * Method to strip the html tags from the summary
     * try escapeHtml if not happy with result
     *
     * @param html string with html tags
     * @return string without html tags
     */
    public String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    public Number getEnrolledusercount() {
        return this.enrolledusercount;
    }

    public void setEnrolledusercount(Number enrolledusercount) {
        this.enrolledusercount = enrolledusercount;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFullname() {
        return this.fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Number getId() {
        return this.id;
    }

    public void setId(Number id) {
        this.id = id;
    }

    public String getLang() {
        return this.lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getShortname() {
        return this.shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public boolean getShowgrades() {
        return this.showgrades;
    }

    public void setShowgrades(boolean showgrades) {
        this.showgrades = showgrades;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Number getSummaryformat() {
        return this.summaryformat;
    }

    public void setSummaryformat(Number summaryformat) {
        this.summaryformat = summaryformat;
    }

    public Number getVisible() {
        return this.visible;
    }

    public void setVisible(Number visible) {
        this.visible = visible;
    }
}
