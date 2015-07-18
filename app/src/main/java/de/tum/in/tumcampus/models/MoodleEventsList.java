package de.tum.in.tumcampus.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * The moodle events list
 */
public class MoodleEventsList extends MoodleObject{
    /**
     * The list of User's Events
     */
    private ArrayList<MoodleEvent> userEvents = new ArrayList<>();

    /**
     *Constructor from json object
     *
     * @param jsonObject JSON in string format
     */
    public MoodleEventsList(JSONObject jsonObject)  {
        try{
            if (jsonObject != null && !jsonObject.has("error")){
                //good
                JSONArray jsonArray = jsonObject.optJSONArray("events");
                if (jsonArray != null){
                    MoodleEvent userEvent;
                    for (int i=0; i < jsonArray.length(); i++){
                        JSONObject event = jsonArray.getJSONObject(i);
                        userEvent = new MoodleEvent(event);

                        userEvents.add(userEvent);
                    }


                } else {

                    this.exception = "EmptyJSONArrayException";
                    this.message = "invalid json object passed to MoodleEventsList model";
                    this.errorCode = "emptyjsonobject";
                    this.isValid=false;

                }

            }

        }
        catch (JSONException e){

            this.userEvents = null;

            this.message="invalid json parsing in MoodleEvent model";
            this.exception="JSONException";
            this.errorCode="invalidjson";
            this.isValid=false;
        }

    }
    public MoodleEventsList(String jsonstring) {
        this(toJSONObject(jsonstring));
    }



    public ArrayList<MoodleEvent> getSections() {
        return userEvents;
    }

    public void setSections(ArrayList<MoodleEvent> sections) {
        this.userEvents = sections;
    }

}
