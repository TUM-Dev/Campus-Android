package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampus.models.Tuition;
import de.tum.in.tumcampus.models.managers.CardManager;


public class TuitionFeesCard extends Card {

    private static final String LAST_FEE_FRIST = "fee_frist";
    private static final String LAST_FEE_SOLL = "fee_soll";
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

    @Override
    public void discard() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LAST_FEE_FRIST, mTuition.getFrist());
        editor.putString(LAST_FEE_SOLL, mTuition.getSoll());
        editor.commit();
    }

    @Override
    public boolean apply() { //TODO: Rethink
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CardManager.getContext());
        String prevFrist = prefs.getString(LAST_FEE_FRIST, "");
        String prevSoll = prefs.getString(LAST_FEE_SOLL, mTuition.getSoll());
        if(prevFrist.compareTo(mTuition.getFrist())<0 || prevSoll.compareTo(mTuition.getSoll())>0) {
            CardManager.addCard(this);
            return true;
        }
        return false;
    }

    public void setTuition(Tuition tuition) {
        mTuition = tuition;
    }
}
