package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

import de.tum.in.tumcampus.models.Tuition;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
* Created by Florian on 17.08.2014.
*/
public class NextLectureCard extends Card {

    private String mTitle;
    private String mDate;
    private String mLocation;

    @Override
    public int getTyp() {
        return CardManager.CARD_NEXT_LECTURE;
    }

    //TODO: translate strings
    @Override
    public View getView(Context context, ViewGroup parent) {
        super.getView(context, parent);
        mTitleView.setText("Nächste Vorlesung");

        int end = mTitle.length();
        if(mTitle.contains("("))
            end = mTitle.indexOf('(');
        addTextView(context, mTitle.substring(0,end).trim()+" am "+mDate+" ("+mLocation+")");

        return mCard;
    }

    public void setLecture(String title, String date, String loc) {
        mTitle = title;
        mDate = date;
        mLocation = loc;
    }
}
