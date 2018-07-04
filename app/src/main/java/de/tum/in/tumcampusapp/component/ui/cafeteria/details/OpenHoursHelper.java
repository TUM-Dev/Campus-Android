package de.tum.in.tumcampusapp.component.ui.cafeteria.details;

import android.content.Context;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaLocationDao;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;

public class OpenHoursHelper {

    private final CafeteriaLocationDao dao;

    OpenHoursHelper(Context context) {
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
    public String getHoursByIdAsString(Context context, int id, DateTime date) {
        String result = dao.getHoursById(id);
        if (result == null) {
            return "";
        }

        //Check which week day we have
        int dayOfWeek = date.getDayOfWeek();

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
        DateTime now = DateTime.now();
        DateTime opens = strToCal(date, time[0]);
        DateTime closes = strToCal(date, time[1]);

        //Check the relativity
        DateTime relativeTo;
        int relation;
        if (opens.isAfter(now)) {
            relation = R.string.opens;
            relativeTo = opens;
        } else if (closes.isAfter(now)) {
            relation = R.string.closes;
            relativeTo = closes;
        } else {
            relation = R.string.closed;
            relativeTo = closes;
        }

        //Get the relative string
        String relativeTime = DateTimeUtils.INSTANCE.formatFutureTime(relativeTo, context);

        //Return an assembly
        return context.getString(relation) + " " + relativeTime.substring(0, 1)
                .toLowerCase(Locale.getDefault()) + relativeTime.substring(1);

    }

    private static DateTime strToCal(DateTime date, String time) {
        DateTime opens = date;
        if (time.contains(".")) {
            int hour = Integer.parseInt(time.substring(0, time.indexOf('.')));
            int min = Integer.parseInt(time.substring(time.indexOf('.') + 1));
            opens = opens
                    .withHourOfDay(hour)
                    .withMinuteOfHour(min);
        } else {
            opens = opens
                    .withHourOfDay(Integer.parseInt(time))
                    .withMinuteOfHour(0);
        }
        return opens;
    }
}