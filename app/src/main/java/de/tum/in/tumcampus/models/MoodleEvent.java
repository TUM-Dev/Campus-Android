
package de.tum.in.tumcampus.models;

import java.util.GregorianCalendar;

import de.tum.in.tumcampus.auxiliary.DateUtils;


/**
 * Created by a2k on 6/8/2015.
 * This class is a representation of an Event for a user in Moodle
 */
public class MoodleEvent {
    private String name;
    private String description;
    private GregorianCalendar date;
    private int duration;

    public MoodleEvent(String name,String description, long date, int duration){
        setName(name);
        setDate(date);
        setDescription(description);
        setDuration(duration);
    }

    public void setName(String name){  this.name = name; }
    public void setDescription(String desc){  this.description = desc; }
    public void setDate(long date){  this.date = DateUtils.epochToDate(date); }
    public void setDuration(int duration){ this.duration = duration;}

    public String getName() {return name;}

    public String getDescription() {
        return description;
    }

    public GregorianCalendar getDate() {
        return date;
    }

    public int getDuration() {
        return duration;
    }

    public String getDateString(){
        return date.getTime().toString();
    }
}
