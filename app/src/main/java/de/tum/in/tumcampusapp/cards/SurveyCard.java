package de.tum.in.tumcampusapp.cards;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;

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
    private SQLiteDatabase db;
    private Button bYes;
    private Button bNo;
    private Button bSkip;
    private ImageButton bFlagged;


    public SurveyCard(Context context) {
        super(context,"card_survey");
    }

    public static Card.CardViewHolder inflateViewHolder(final ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_test, parent, false);
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
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        mCard = viewHolder.itemView;
        mLinearLayout = (LinearLayout) mCard.findViewById(R.id.card_view);
        mTitleView = (TextView) mCard.findViewById(R.id.card_title);
        mQuestion = (TextView) mCard.findViewById(R.id.questionText);
        bYes = (Button) mCard.findViewById(R.id.yesAnswerCard);
        bNo = (Button) mCard.findViewById(R.id.noAnswerCard);
        bSkip= (Button) mCard.findViewById(R.id.ignoreAnswerCard);
        bFlagged = (ImageButton) mCard.findViewById(R.id.flagButton);

        showQuestion(0);

    }

    public void showQuestion(int i) {
        mTitleView.setText("Research Quiz");
        final Question ques = questions.get(i);
        mQuestion.setText(ques.question);


        // Listens on the yes button in the card
        bYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( questions.size() >=2){
                    Question updatedElement = questions.remove(0);
                    manager.updateQuestion(ques,"yes");
                    // show next question
                    showQuestion(0);
                }else{
                    // no questions available
                    Question updateElement = questions.remove(0);
                    manager.updateQuestion(ques,"yes");
                    mQuestion.setText("No Questions are available!");
                    bYes.setVisibility(View.GONE);
                    bNo.setVisibility(View.GONE);
                    bSkip.setVisibility(View.GONE);
                    bFlagged.setVisibility(View.GONE);
                }
            }
        });
        bNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( questions.size() >=2){
                    Question updatedElement = questions.remove(0);
                    manager.updateQuestion(ques,"no");
                    showQuestion(0);
                }else{
                    Question updateElement = questions.remove(0);
                    manager.updateQuestion(ques,"no");
                    mQuestion.setText("No Questions are available!");
                    bYes.setVisibility(View.GONE);
                    bNo.setVisibility(View.GONE);
                    bSkip.setVisibility(View.GONE);
                    bFlagged.setVisibility(View.GONE);
                }
            }
        });
        bSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( questions.size() >=2){
                    Question updatedElement = questions.remove(0);
                    showQuestion(0);
                }else{
                    Question updateElement = questions.remove(0);
                    mQuestion.setText("No Questions are available!");
                    bYes.setVisibility(View.GONE);
                    bNo.setVisibility(View.GONE);
                    bSkip.setVisibility(View.GONE);
                    bFlagged.setVisibility(View.GONE);
                }
            }
        });
        bFlagged.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( questions.size() >=2){
                    Question updatedElement = questions.remove(0);
                    // missing: update the database
                    manager.updateQuestion(ques,"flagged");
                    showQuestion(0);
                }else{
                    Question updateElement = questions.remove(0);
                    manager.updateQuestion(ques,"flagged");
                    mQuestion.setText("No Questions are available!");
                    bYes.setVisibility(View.GONE);
                    bNo.setVisibility(View.GONE);
                    bSkip.setVisibility(View.GONE);
                    bFlagged.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public boolean shouldShow(SharedPreferences p) {
        return manager.getNextQuestions().getCount() >= 1;
    }

    public void seQuestions(Cursor cur) {
        do {
            Question item = new Question();
            item.questionID = cur.getInt(0);
            item.question = cur.getString(1);
            questions.add(item);
        } while (cur.moveToNext());
        cur.close();
    }


    public class Question {
        int questionID;
        String question;
        int yes;
        int no;
        int flagged;
        int answered;
        int synced;

        public int getQuestionID(){
            return questionID;
        }

        public String getQuestion(){
            return question;
        }
    }
}
