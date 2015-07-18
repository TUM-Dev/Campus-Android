package de.tum.in.tumcampus.models;


import android.text.Html;

import org.json.JSONObject;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import de.tum.in.tumcampus.auxiliary.DateUtils;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * The moodle event
 */
public class MoodleEvent extends MoodleObject {


    private Integer id;
    private String name;
    private String description;
    private Integer format;
    private Integer courseid;
    private Integer groupid;
    private Integer userid;
    private Integer repeatid;
    private String modulename;
    private Integer instance;
    private String eventtype;
    private Integer timestart;
    private Integer timeduration;
    private Integer visible;
    private String uuid;
    private Integer sequence;
    private Integer timemodified;
    private Integer subscriptionid;
    private Map<String, Object> additionalProperties = new HashMap<>();


    public MoodleEvent(String jsonString) {
        this(toJSONObject(jsonString));
    }

    public MoodleEvent(JSONObject jsonObject) {

        if (jsonObject != null && !jsonObject.has("error")) {

            setId(jsonObject.optInt("id"));
            setName(jsonObject.optString("name"));
            setDescription(jsonObject.optString("description"));
            setFormat(jsonObject.optInt("format"));
            if (getFormat().doubleValue() == 1) {
                setDescription(stripHtml(getDescription()));
            }
            setCourseid(jsonObject.optInt("courseid"));
            setGroupid(jsonObject.optInt("groupid"));
            setRepeatid(jsonObject.optInt("repeatid"));
            setModulename(jsonObject.optString("modulename"));
            setInstance(jsonObject.optInt("instance"));
            setEventtype(jsonObject.optString("eventtype"));
            setTimestart(jsonObject.optInt("timestart"));
            setTimeduration(jsonObject.optInt("timeduration"));
            setVisible(jsonObject.optInt("visible"));
            setUuid(jsonObject.optString("uuid"));
            setSequence(jsonObject.optInt("sequence"));
            setTimemodified(jsonObject.optInt("timemodified"));
            setSubscriptionid(jsonObject.optInt("subscriptionid"));
        } else {

            this.isValid = false;
            this.exception = "JSONException";
            this.errorCode = "invalidjsonstring";
            this.message = "error while parsing jsonstring in " + this.getClass().getName();
        }

    }

    /**
     * @return The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The format
     */
    public Integer getFormat() {
        return format;
    }

    /**
     * @param format The format
     */
    public void setFormat(Integer format) {
        this.format = format;
    }

    /**
     * @return The courseid
     */
    public Integer getCourseid() {
        return courseid;
    }

    /**
     * @param courseid The courseid
     */
    public void setCourseid(Integer courseid) {
        this.courseid = courseid;
    }

    /**
     * @return The groupid
     */
    public Integer getGroupid() {
        return groupid;
    }

    /**
     * @param groupid The groupid
     */
    public void setGroupid(Integer groupid) {
        this.groupid = groupid;
    }

    /**
     * @return The userid
     */
    public Integer getUserid() {
        return userid;
    }

    /**
     * @param userid The userid
     */
    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    /**
     * @return The repeatid
     */
    public Integer getRepeatid() {
        return repeatid;
    }

    /**
     * @param repeatid The repeatid
     */
    public void setRepeatid(Integer repeatid) {
        this.repeatid = repeatid;
    }

    /**
     * @return The modulename
     */
    public String getModulename() {
        return modulename;
    }

    /**
     * @param modulename The modulename
     */
    public void setModulename(String modulename) {
        this.modulename = modulename;
    }

    /**
     * @return The instance
     */
    public Integer getInstance() {
        return instance;
    }

    /**
     * @param instance The instance
     */
    public void setInstance(Integer instance) {
        this.instance = instance;
    }

    /**
     * @return The eventtype
     */
    public String getEventtype() {
        return eventtype;
    }

    /**
     * @param eventtype The eventtype
     */
    public void setEventtype(String eventtype) {
        this.eventtype = eventtype;
    }

    /**
     * @return The timestart
     */
    public Integer getTimestart() {
        return timestart;
    }

    /**
     * @param timestart The timestart
     */
    public void setTimestart(Integer timestart) {
        this.timestart = timestart;
    }

    /**
     * @return The timeduration
     */
    public Integer getTimeduration() {
        return timeduration;
    }

    /**
     * @param timeduration The timeduration
     */
    public void setTimeduration(Integer timeduration) {
        this.timeduration = timeduration;
    }

    /**
     * @return The visible
     */
    public Integer getVisible() {
        return visible;
    }

    public static GregorianCalendar getDate(String eventDateString) {
        /**
         * gets a DateString created by event class and returns
         * an GregorianCalendar for date inside the String
         * @param eventDateString String created by an event object
         * @return GregorianCalendar
         */
        String[] values = eventDateString.split("\n");
        String dateString = values[0];
        Date date = DateUtils.parseSimpleDateFormat(dateString);
        if (date != null) {
            GregorianCalendar g = new GregorianCalendar();
            g.setTime(date);
            return g;
        } else {
            Utils.log("#error: failed to convert to GregorianCalendar: " + eventDateString);
            return null;
        }
    }

    /**
     * @param visible The visible
     */
    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    /**
     * @return The uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid The uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return The sequence
     */
    public Integer getSequence() {
        return sequence;
    }

    /**
     * @param sequence The sequence
     */
    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    /**
     * @return The timemodified
     */
    public Integer getTimemodified() {
        return timemodified;
    }

    /**
     * @param timemodified The timemodified
     */
    public void setTimemodified(Integer timemodified) {
        this.timemodified = timemodified;
    }

    /**
     * @return The subscriptionid
     */
    public Integer getSubscriptionid() {
        return subscriptionid;
    }

    /**
     * @param subscriptionid The subscriptionid
     */
    public void setSubscriptionid(Integer subscriptionid) {
        this.subscriptionid = subscriptionid;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

}
