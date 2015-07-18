package de.tum.in.tumcampus.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enricogiga on 05/06/2015.
 * Object holding an element in a Moodle course. It can be anything, a video resource, a link, a file, a section header  ecc
 * the kind of object is described in the modname and for each there is an icon to retrieve in at the modicon URL.
 * The module can be also a holder of contents which can once again be any kind of thing
 * TODO: manage and cache the images in modicon (we might just download all of them locally, they can't be to much)
 * used in the array of modules of a course
 */
public class MoodleCourseModule extends MoodleObject{
    /**
     * Module's id
     */
    private Number id;
    /**
     * no idea
     */
    private Number instance;
    /**
     * Module's url for the icon, not set as URL now
     * there is one for each modname
     */
    private String modicon;
    /**
     * distinguishes the type of content of the module
     * it is almost always a link but it can be a downloadable file to which we
     * have to append a token.
     * These the types I could notice
     * Forum, Choice, Quiz, Chat, Glossary, Label, Book, Resource, Folder,
     *  Assign, Survey, Wiki, Feedback, Data, Workshop, Math
     *
     *  NB: Label is just text, might be used as section header
     *      Resource, Book, Folder might hold an array of contents
     **/
    private String modname;
    /**
     * Name of the modname in user's language
     */
    private String modplural;
    /**
     * Name of the module
     */
    private String name;
    /**
     * URL to the resource
     * this parameter is not in the modules with modname=label
     */
    private URL url;
    /**
     * no idea
     */
    private Number visible;
    /**
     * List of MoodleCourseContents
     * there is no heuristic to know if it will be there or not
     * might always want to check if null before accessing it
     * by now i noticed Resource, Book, Folder always have it
     */
    private List <MoodleCourseContent> contents = new ArrayList<MoodleCourseContent>();



    /**
     * Constructor to use if JSON is passed as a string
     * @param jsonString JSON in string format
     */
    public MoodleCourseModule(String jsonString) {
        this(toJSONObject(jsonString));
    }

    /**
     * Constructor by parsing the JSONObject
     * @param jsonObject the JSONObject
     */
    public MoodleCourseModule(JSONObject jsonObject){
        this.id=null;
        this.instance=null;
        this.modicon=null;
        this.modname=null;
        this.modplural=null;
        this.name=null;
        this.url=null;
        this.visible=null;
        this.contents=null;

        if (jsonObject != null){
            if (!jsonObject.has("exception")) {
                this.instance = jsonObject.optInt("instance");
                this.id = jsonObject.optInt("id");
                this.modicon = jsonObject.optString("modicon");
                this.modname = jsonObject.optString("modname");
                this.name = jsonObject.optString("name");
                this.modplural = jsonObject.optString("modplural");
                this.visible = jsonObject.optDouble("visible");
                try{
                    this.url = new URL(jsonObject.optString("url"));
                }
                catch (MalformedURLException m){
                    this.url=null;
                    this.message=m.getMessage();
                    this.exception="MalformedURLException";
                    this.errorCode="malformedurl";

                }


                MoodleCourseContent content;
                try{
                    //JSONArray jsonArray = jsonObject.getJSONArray("modules");
                    JSONArray jsonArray = jsonObject.getJSONArray("contents");
                    this.contents = new ArrayList<>();
                    for (int i=0; i < jsonArray.length(); i++){
                        JSONObject c = jsonArray.getJSONObject(i);
                        content = new MoodleCourseContent(c);
                        contents.add(content);
                    }

                } catch (JSONException e){
                    this.contents = null;

                    this.message="invalid json parsing in MoodleCourseModule model";
                    this.exception="JSONException";
                    this.errorCode="invalidjson";
                    this.isValid=false;
                }

            } else{
                this.exception = jsonObject.optString("exception");
                this.errorCode = jsonObject.optString("errorcode");
                this.message = jsonObject.optString("message");
                this.isValid=false;

            }

        } else {
            this.exception = "EmptyJSONObjectException";
            this.message = "invalid json object passed to MoodleUserCourse model";
            this.errorCode = "emptyjsonobject";
            this.isValid=false;

        }

    }

    public Number getId(){
        return this.id;
    }

    public List<MoodleCourseContent> getContents() {
        return contents;
    }

    public void setContents(List<MoodleCourseContent> contents) {
        this.contents = contents;
    }

    public void setId(Number id){

        this.id = id;
    }

    public Number getInstance(){
        return this.instance;
    }
    public void setInstance(Number instance){
        this.instance = instance;
    }
    public String getModicon(){
        return this.modicon;
    }
    public void setModicon(String modicon){
        this.modicon = modicon;
    }
    public String getModname(){
        return this.modname;
    }
    public void setModname(String modname){
        this.modname = modname;
    }
    public String getModplural(){
        return this.modplural;
    }
    public void setModplural(String modplural){
        this.modplural = modplural;
    }
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }
    public URL getUrl(){
        return this.url;
    }
    public void setUrl(URL url){
        this.url = url;
    }
    public Number getVisible(){
        return this.visible;
    }
    public void setVisible(Number visible){
        this.visible = visible;
    }
}
