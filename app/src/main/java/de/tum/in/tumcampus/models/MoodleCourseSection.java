package de.tum.in.tumcampus.models;

/**
 * Created by enricogiga on 05/06/2015.
 * Object holding the information on a section of a course (components of the course main page on moodle)
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MoodleCourseSection extends MoodleObject {
    /**
     * section's id
     */
    private Number id;
    /**
     * List of MoodleCourseModules
     */
    private List<MoodleCourseModule> modules = new ArrayList<>();
    /**
     * section's name
     */
    private String name;
    /**
     * section's summary might be null
     */
    private String summary;
    /**
     * section's summary format html=1
     */
    private Number summaryformat;
    /**
     * no idea
     */
    private Number visible;


    /**
     * Constructor of the object from JSONObject
     *
     * @param jsonObject = JSONObject to parse
     */
    public MoodleCourseSection(JSONObject jsonObject) {
        this.id = null;
        this.modules = new ArrayList<>();
        this.name = null;
        this.summary = null;
        this.visible = null;
        this.summaryformat = null;
        if (jsonObject != null) {
            if (!jsonObject.has("exception")) {


                MoodleCourseModule module;
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("modules");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);
                        module = new MoodleCourseModule(c);
                        modules.add(module);
                    }

                } catch (JSONException e) {
                    this.modules = null;

                    this.message = "invalid json parsing in MoodleCourseSection model";
                    this.exception = "JSONException";
                    this.errorCode = "invalidjson";
                    this.isValid = false;
                }

                this.id = jsonObject.optInt("id");
                //this.name = jsonObject.optString("format");
                this.name = jsonObject.optString("name");
                this.summary = jsonObject.optString("summary");
                this.summaryformat = jsonObject.optDouble("summaryformat");
                this.visible = jsonObject.optDouble("visible");
            } else {
                this.exception = jsonObject.optString("exception");
                this.errorCode = jsonObject.optString("errorcode");
                this.message = jsonObject.optString("message");
                this.isValid = false;

            }

        } else {
            this.exception = "EmptyJSONObjectException";
            this.message = "invalid json object passed to MoodleCourseSection model";
            this.errorCode = "emptyjsonobject";
            this.isValid = false;

        }

    }

    public Number getId() {
        return this.id;
    }

    public void setId(Number id) {
        this.id = id;
    }

    public List<MoodleCourseModule> getModules() {
        return this.modules;
    }

    public void setModules(List<MoodleCourseModule> modules) {
        this.modules = modules;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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
