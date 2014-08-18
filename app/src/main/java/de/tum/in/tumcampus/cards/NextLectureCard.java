package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import de.tum.in.tumcampus.R;
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
    public View getView(Context context, ViewGroup parent) {
        super.getView(context, parent);
        mTitleView.setText(context.getString(R.string.next_lecture));
        final String time = DateUtils.getRelativeDateTimeString(context,mDate.getTime(),
                DateUtils.MINUTE_IN_MILLIS,DateUtils.WEEK_IN_MILLIS,0).toString();
        addTextView(context,mTitle+"\n"+time);

        return mCard;
    }

    public void setLecture(String title, String date, String loc) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        int end = title.indexOf('(');
        if(end<0)
            end = title.length();
        mTitle = title.substring(0, end).trim();
        try {
            mDate = formatter.parse(date);
        } catch (ParseException e) {
            mDate = null;
        }
        mLocation = loc;
    }
}
