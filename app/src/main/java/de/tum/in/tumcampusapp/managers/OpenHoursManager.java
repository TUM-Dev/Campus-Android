package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.DateUtils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dao.LocationDao;

/**
 * Location manager, handles database stuff
 */
public class OpenHoursManager {

    private final LocationDao dao;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public OpenHoursManager(Context context) {
        dao = TcaDb.getInstance(context)
                   .locationDao();
    }

    /**
     * Converts the opening hours into more readable format.
     * e.g. Opening in 2 hours.
     * HINT: Currently only works for cafeterias, and institutions
     * that have Mo-Do xx-yy.yy, Fr aa-bb and Mo-Fr xx-yy format
     *
     * @param context Context
     * @param id      Location ID, e.g. 100
     * @param date    Relative date
     * @return Readable opening string
     */
    public String getHoursByIdAsString(Context context, int id, Date date) {
        String result = dao.getHoursById(id);

        //Check which week day we have
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        //Split up the data string from the database with regex which has the format: "Mo-Do 11-14, Fr 11-13.45" or "Mo-Fr 9-20"
        Matcher m = Pattern.compile("([a-z]{2}?)[-]?([a-z]{2}?)? ([0-9]{1,2}(?:[\\.][0-9]{2}?)?)-([0-9]{1,2}(?:[\\.][0-9]{2}?)?)", Pattern.CASE_INSENSITIVE)
                           .matcher(result);

        //Capture groups for: Mo-Do 9-21.30
        //#0	Mo-Do 9-21.30
        //#1	Mo
        //#2	Do
        //#3	9
        //#4	21.30

        //Find the first part
        String[] time = new String[2];
        if (m.find()) {
            //We are currently in Mo-Do/Fr, when this weekday is in that range we have our result or we check if the current range is valid for fridays also
            if (dayOfWeek <= Calendar.THURSDAY || m.group(2)
                                                   .equalsIgnoreCase("fr")) {
                time[0] = m.group(3);
                time[1] = m.group(4);
            } else {
                //Otherwise we need to move to the next match
                if (m.find()) {
                    //Got a match, data should be in capture groups 3/4
                    time[0] = m.group(3);
                    time[1] = m.group(4);
                } else {
                    //No match found, return
                    return "";
                }
            }
        } else {
            //No match found, return
            return "";
        }

        //Convert time to workable calender objects
        Calendar now = Calendar.getInstance();
        Calendar opens = strToCal(date, time[0]);
        Calendar closes = strToCal(date, time[1]);

        //Check the relativity
        Calendar relativeTo;
        int relation;
        if (opens.after(now)) {
            relation = R.string.opens;
            relativeTo = opens;
        } else if (closes.after(now)) {
            relation = R.string.closes;
            relativeTo = closes;
        } else {
            relation = R.string.closed;
            relativeTo = closes;
        }

        //Get the relative string
        String relStr = DateUtils.getFutureTime(relativeTo.getTime(), context);

        //Return an assembly
        return context.getString(relation) + " " + relStr.substring(0, 1)
                                                         .toLowerCase(Locale.getDefault()) + relStr.substring(1);

    }

    private static Calendar strToCal(Date date, String time) {
        Calendar opens = Calendar.getInstance();
        opens.setTime(date);
        if (time.contains(".")) {
            int hour = Integer.parseInt(time.substring(0, time.indexOf('.')));
            int min = Integer.parseInt(time.substring(time.indexOf('.') + 1));
            opens.set(Calendar.HOUR_OF_DAY, hour);
            opens.set(Calendar.MINUTE, min);
        } else {
            opens.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time));
            opens.set(Calendar.MINUTE, 0);
        }
        return opens;
    }
}