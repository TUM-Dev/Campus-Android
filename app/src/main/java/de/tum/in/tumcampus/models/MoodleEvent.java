
package de.tum.in.tumcampus.models;

import java.util.Date;
import java.util.GregorianCalendar;

import de.tum.in.tumcampus.auxiliary.DateUtils;
import de.tum.in.tumcampus.auxiliary.Utils;


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
        String time = date.getTime().toString();
        int minutes = duration / 60;
        String duration = "duration: " + String.valueOf(minutes) + " min ";
        return date.getTime().toString() + "\n" + duration;
    }

    public static int getDuration(String eventDateString){
        /**
         * gets a DateString created by event class and returns
         * an integer value for seconds of duration inside the String
         * @param eventDateString String created by an event object
         * @return an int, representing the amount of duration in seconds
         */
        String [] values = eventDateString.split("\n");
        String duration = values[1];
        duration = duration.replaceAll("[^0-9]","");
        return Integer.valueOf(duration) * 60;
    }

    public static GregorianCalendar getDate(String eventDateString){
        /**
         * gets a DateString created by event class and returns
         * an GregorianCalendar for date inside the String
         * @param eventDateString String created by an event object
         * @return GregorianCalendar
         */
        String [] values = eventDateString.split("\n");
        String dateString = values[0];
        Date date = DateUtils.parseSimpleDateFormat(dateString);
        if (date!=null) {
            GregorianCalendar g = new GregorianCalendar();
            g.setTime(date);
            return g;
        }else {
            Utils.log("#error: failed to convert to GregorianCalendar: "  + eventDateString);
            return null;
        }

    }
}
