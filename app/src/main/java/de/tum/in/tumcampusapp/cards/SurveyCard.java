package de.tum.in.tumcampusapp.cards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.managers.CardManager;
import de.tum.in.tumcampusapp.managers.SurveyManager;
import de.tum.in.tumcampusapp.models.dbEntities.OpenQuestions;
import de.tum.in.tumcampusapp.models.tumcabe.Question;

public class SurveyCard extends Card {
    private static final String SURVEY_CARD_DISCARDED_TILL = "survey_card_discarded_till";
    private final List<Question> questions = new ArrayList<>(); // gets filled with the relevant openQuestions for the card
    private final SurveyManager manager = new SurveyManager(mContext);
    private TextView mQuestion;
    private Button bYes;
    private Button bNo;
    private Button bSkip;
    private ImageButton bFlagged;
    private final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); // For converting Jade DateTime into String & vic versa (see show and discard functions)
    // Answer flags relevant for updating the answered questions in the db

    private static final int answerYes = 1;
    private static final int answerNo = 2;
    private static final int answerFlag = -1;
    private static final int answerSkip = 3;

    public SurveyCard(Context context) {
        super(CardManager.CARD_SURVEY, context, "card_survey");
    }

    public static Card.CardViewHolder inflateViewHolder(final ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.card_survey, parent, false);
        return new Card.CardViewHolder(view);
    }

    /**
     * Handles the changing content of the survey card
     *
     * @param viewHolder The Card specific view holder
     */
    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        mCard = viewHolder.itemView;
        mLinearLayout = mCard.findViewById(R.id.card_view);
        mTitleView = mCard.findViewById(R.id.card_title);
        mQuestion = mCard.findViewById(R.id.questionText);
        bYes = mCard.findViewById(R.id.yesAnswerCard);
        bNo = mCard.findViewById(R.id.noAnswerCard);
        bSkip = mCard.findViewById(R.id.ignoreAnswerCard);
        bFlagged = mCard.findViewById(R.id.flagButton);

        showFirstQuestion();

    }

    /**
     * 1. Updates the answered question in the db
     * 2. Changes the content of the survey card depending on the questions ArrayList
     */
    private void showFirstQuestion() {
        mTitleView.setText(R.string.research_quiz);

        if (!questions.isEmpty()) {
            final Question ques = questions.get(0);
            mQuestion.setText(ques.getText()); // Sets the text of the question that should be shown first

            // TODO: this can probably be replaced by a single OnClickListener
            // Listens on the yes button in the card
            bYes.setOnClickListener(view -> {
                Question updatedElement = questions.remove(0);
                manager.updateQuestion(updatedElement, answerYes); // update the answerID in the local db.
                showNextQuestions(); // handel showing next question(s)
            });
            bNo.setOnClickListener(view -> {
                Question updatedElement = questions.remove(0);
                manager.updateQuestion(updatedElement, answerNo); // update the answerID in the local db.
                showNextQuestions(); // handel showing next question(s)
            });
            bSkip.setOnClickListener(view -> {
                Question updatedElement = questions.remove(0);
                manager.updateQuestion(updatedElement, answerSkip); // update the answerID in the local db.
                showNextQuestions(); // handel showing next question(s)
            });
            bFlagged.setOnClickListener(view -> {
                Question updatedElement = questions.remove(0);
                manager.updateQuestion(updatedElement, answerFlag); // update the answerID in the local db.
                showNextQuestions(); // handel showing next question(s)
            });
        }
    }

    /**
     * Help function which calls showFirstQuestion() recursively
     * depending on the size of the question Array list
     */
    private void showNextQuestions() {
        if (questions.isEmpty()) { // show there are no questions available anymore
            mQuestion.setText(R.string.no_questions_available);
            bYes.setVisibility(View.GONE);
            bNo.setVisibility(View.GONE);
            bSkip.setVisibility(View.GONE);
            bFlagged.setVisibility(View.GONE);
        } else {
            // if the question arraylist is not empty, show the first question (the answered question before got removed from the list)
            showFirstQuestion();
        }
    }

    /**
     * Handles discarding the survey card. Grace period of 24 hours
     * Card should be shown again depending on the next function
     *
     * @param editor Editor to be used for saving values
     */
    @Override
    public void discard(SharedPreferences.Editor editor) {
        DateTime discardedTill = DateTime.now()
                                         .plusMinutes(1440); // in 24 hours
        String discardTimeString = discardedTill.toString(fmt);
        editor.putString(SURVEY_CARD_DISCARDED_TILL, discardTimeString);
    }

    /**
     * Shows the card if there are relevant unansweredQuestions (not expired)
     * AND the discard grace period (if there is any) is finished
     */
    @Override
    protected boolean shouldShow(SharedPreferences p) {
        String currentDate = Utils.getDateTimeString(new Date());
        DateTime discardedTill = fmt.parseDateTime(p.getString(SURVEY_CARD_DISCARDED_TILL, DateTime.now()
                                                                                                   .toString(fmt)));
        List<OpenQuestions> unansweredQuestions = manager.getUnansweredQuestionsSince(currentDate);
        return discardedTill.isBeforeNow() &&
                   unansweredQuestions.size() >= 1;
    }

    @Override
    public Intent getIntent() {
        return null;
    }

    @Override
    public int getId() {
        return 0;
    }

    /**
     * Sets the open questions (fetched from the server) in the  card
     *
     */
    public void setQuestions(List<OpenQuestions> unansweredQuestions) {
        for (OpenQuestions unansweredQuestion: unansweredQuestions) {
            Question item = new Question(Integer.toString(unansweredQuestion.getQuestion()),
                                         unansweredQuestion.getText());
            questions.add(item);
        }
    }
}
