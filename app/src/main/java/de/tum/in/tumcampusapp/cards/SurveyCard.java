package de.tum.in.tumcampusapp.cards;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.Question;
import de.tum.in.tumcampusapp.models.managers.CardManager;
import de.tum.in.tumcampusapp.models.managers.SurveyManager;

public class SurveyCard extends Card

{
    private final ArrayList<Question> questions = new ArrayList<>();
    private final SurveyManager manager = new SurveyManager(mContext);
    private TextView mQuestion;
    private Button bYes;
    private Button bNo;
    private Button bSkip;
    private ImageButton bFlagged;


    public SurveyCard(Context context) {
        super(context, "card_survey");
    }

    public static Card.CardViewHolder inflateViewHolder(final ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_survey, parent, false);
        return new Card.CardViewHolder(view);
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_SURVEY;
    }

    @Override
    public void discard(SharedPreferences.Editor editor) {
        //@todo
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
        bSkip = (Button) mCard.findViewById(R.id.ignoreAnswerCard);
        bFlagged = (ImageButton) mCard.findViewById(R.id.flagButton);

        showFirstQuestion();

    }

    private void showFirstQuestion() {
        mTitleView.setText(R.string.research_quiz);
        if (!questions.isEmpty()) {
            final Question ques = questions.get(0);
            mQuestion.setText(ques.getText());

            // Listens on the yes button in the card
            bYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Question updatedElement = questions.remove(0);
                    manager.updateQuestion(updatedElement, "yes");
                    showNextQuestions();
                }
            });
            bNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Question updatedElement = questions.remove(0);
                    manager.updateQuestion(updatedElement, "no");
                    showNextQuestions();
                }
            });
            bSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Question updatedElement = questions.remove(0);
                    manager.updateQuestion(updatedElement, "answered");
                    showNextQuestions();
                }
            });
            bFlagged.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Question updatedElement = questions.remove(0);
                    manager.updateQuestion(updatedElement, "flagged");
                    showNextQuestions();
                }
            });
        }
    }

    private void showNextQuestions() {
        if (questions.size() >= 1) {
            showFirstQuestion();
        } else {
            mQuestion.setText(R.string.no_questions_available);
            bYes.setVisibility(View.GONE);
            bNo.setVisibility(View.GONE);
            bSkip.setVisibility(View.GONE);
            bFlagged.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean shouldShow(SharedPreferences p) {
        return manager.getUnansweredQuestions().getCount() >= 1;
    }

    public void seQuestions(Cursor cur) {
        do {
            Question item = new Question(cur.getString(0), cur.getString(1), false);
            questions.add(item);
        } while (cur.moveToNext());
        cur.close();
    }
}
