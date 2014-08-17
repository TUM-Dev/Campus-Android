package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampus.models.Tuition;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
* Created by Florian on 17.08.2014.
*/
public class TuitionFeesCard extends Card {

    private Tuition mTuition;

    @Override
    public int getTyp() {
        return CardManager.CARD_TUITION_FEE;
    }

    //TODO: translate strings
    @Override
    public View getView(Context context, ViewGroup parent) {
        super.getView(context, parent);
        mTitleView.setText("Rückmeldung");

        if(mTuition.getSoll().equals("0")) {
            addTextView(context, "Sie sind für das "+mTuition.getSemesterBez()+" rückgemeldet!");
        } else {
            addTextView(context, mTuition.getSoll()+"€");
            addTextView(context, "Sie können sich bis zum "+mTuition.getFrist()+" rückmelden!");
        }

        return mCard;
    }

    public void setTuition(Tuition tuition) {
        mTuition = tuition;
    }
}
