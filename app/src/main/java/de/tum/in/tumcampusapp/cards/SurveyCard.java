package de.tum.in.tumcampusapp.cards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.CardManager;
import de.tum.in.tumcampusapp.models.managers.SurveyManager;

/**
 * Created by Moh on 4/26/2016.
 */
public class SurveyCard extends Card

{

    private ArrayList<Question> questions = new ArrayList<>();
    private SurveyManager manager = new SurveyManager(mContext);
    private TextView mQuestion;


    public SurveyCard(Context context) {
        super(context,"card_survey");
    }

    public static Card.CardViewHolder inflateViewHolder(final ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_test, parent, false);
        //Add links to imageviews
       /*view.findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }

        });*/

        return new Card.CardViewHolder(view);
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_SURVEY;
    }

    @Override
    public void discard(SharedPreferences.Editor editor) {
        Utils.setSetting(mContext, CardManager.SHOW_TEST, false);
    }

    @Override
    public boolean shouldShow(SharedPreferences p) {
        return manager.getNextQuestions().getCount() >= 1;
    }

    public void seQuestions(Cursor cur) {
        do {
            Question item = new Question();
            item.question = cur.getString(0);
            questions.add(item);
        } while (cur.moveToNext());
        cur.close();
    }

    private class Question {
        String question;
    }
}
