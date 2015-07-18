package de.tum.in.tumcampus.models;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by enricogiga on 05/06/2015.
 * Fill's in the user information which is retrieved by the call to the moodle function core_webservice_get_site_info when a
 * token is passed to it.
 * This class will hold the URL to the profile picture and user data
 * I'm not sure if this is the way but essentially it was the only function from moodle which gives back a user id when passed a token
 */
public class MoodleUser extends MoodleObject {


    /**
     * user's first name
     */
    private String firstname;
    /**
     * user's full name
     */
    private String fullname;
    /**
     * user's language "IT" format
     */
    private String lang;
    /**
     * user's last name
     */
    private String lastname;
    /**
     * user's id
     */
    private Number userid;
    /**
     * user's username
     */
    private String username;
    /**
     * user's profile picture's URL
     * May throw exception while parsing but I've decided to just set
     * the URL to null and give the rest of the content anyways
     */
    private URL userpictureurl;


    /**
     * Constructor to use if JSON is passed as a string
     *
     * @param jsonstring the json
     */
    public MoodleUser(String jsonstring) {
        this(toJSONObject(jsonstring));

    }

    /**
     * Constructor by paring the JSONObject
     *
     * @param jsonObject the JSONObject
     */
    public MoodleUser(JSONObject jsonObject) {
        this.firstname = null;
        this.lastname = null;
        this.fullname = null;
        this.lang = null;
        this.userid = null;
        this.username = null;
        this.userpictureurl = null;
        if (jsonObject != null) {
            if (!jsonObject.has("exception")) {
                this.firstname = jsonObject.optString("firstname");
                this.lastname = jsonObject.optString("lastname");
                this.fullname = jsonObject.optString("fullname");
                this.lang = jsonObject.optString("lang");
                this.userid = jsonObject.optInt("userid");
                this.username = jsonObject.optString("username");
                try {
                    this.userpictureurl = new URL(jsonObject.optString("userpictureurl"));
                } catch (MalformedURLException m) {
                    this.userpictureurl = null;
                    this.message = m.getMessage();
                    this.exception = "MalformedURLException";
                    this.errorCode = "malformedurl";
                }
            } else {
                this.isValid = false;
                this.exception = jsonObject.optString("exception");
                this.errorCode = jsonObject.optString("errorcode");
                this.message = jsonObject.optString("message");
            }
        } else {
            this.isValid = false;
            this.exception = "JSONException";
            this.errorCode = "invalidjsonstring";
            this.message = "error while parsing jsonstring in " + this.getClass().getName();
        }
    }

    public MoodleUser() {
        super();
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Number getUserid() {
        return userid;
    }

    public void setUserid(Number userid) {
        this.userid = userid;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public URL getUserpictureurl() {
        return userpictureurl;
    }

    public void setUserpictureurl(URL userpictureurl) {
        this.userpictureurl = userpictureurl;
    }
}
