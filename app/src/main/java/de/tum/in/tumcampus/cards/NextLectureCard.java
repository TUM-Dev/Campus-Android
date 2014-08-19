package de.tum.in.tumcampus.cards;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.RoomfinderActivity;
import de.tum.in.tumcampus.models.Tuition;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
 * Created by Florian on 17.08.2014.
 */
public class NextLectureCard extends Card {

    private String mTitle;
    private Date mDate;
    private String mLocation;

    @Override
    public int getTyp() {
        return CardManager.CARD_NEXT_LECTURE;
    }

    @Override
    public View getView(final Context context, ViewGroup parent) {
        super.getView(context, parent);
        mTitleView.setText(context.getString(R.string.next_lecture));

        //Add content
        final String time = DateUtils.getRelativeDateTimeString(context, mDate.getTime(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
        addTextView(context, mTitle + "\n" + time);

        //Add location with link to room finder
        if(mLocation!=null){
            TextView location=addTextView(context, context.getString(R.string.room)+": "+mLocation);
            location.setTextColor(context.getResources().getColor(R.color.holo_blue_bright));
            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, RoomfinderActivity.class);
                    i.setAction(Intent.ACTION_SEARCH);
                    i.putExtra(SearchManager.QUERY, mLocation);
                    context.startActivity(i);
                }
            });
        }



        return mCard;
    }

    public void setLecture(String title, String date, String loc) {
        // Extract course title
        int end = title.indexOf('(');
        if (0 < end) {
            end = title.length();
        }
        mTitle = title.substring(0, end).trim();

        // Format Date
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            mDate = formatter.parse(date);
        } catch (ParseException e) {
            mDate = null;
        }

        // Check for Location
        mLocation = loc;
    }
}
