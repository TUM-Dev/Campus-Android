package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.managers.CardManager;

public abstract class Card {
    private static final String DISCARD_SETTINGS = "discard_settings";
    protected TextView mTitleView;
    protected TextView mDateView;
    protected View mCard;
    protected LinearLayout mLinearLayout;
    protected LayoutInflater mInflater;
    protected Context mContext;

    public abstract int getTyp();
    public View getView(Context context, ViewGroup parent) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCard = mInflater.inflate(R.layout.card_item, parent, false);
        mLinearLayout = (LinearLayout) mCard.findViewById(R.id.card_view);
        mTitleView = (TextView) mCard.findViewById(R.id.card_title);
        mDateView = (TextView) mCard.findViewById(R.id.card_date);
        return mCard;
    }

    public TextView addTextView(Context context, String text) {

        TextView textview = new TextView(context);
        textview.setText(text);

        //Give some space to the other stuff on the card
        float density = context.getResources().getDisplayMetrics().density;
        textview.setPadding((int)(10*density), 0, (int)(10*density), 0);

        mLinearLayout.addView(textview);

        return textview;
    }

    protected void addHeader(Context context, String title) {
        View view = mInflater.inflate(R.layout.card_list_header, mLinearLayout, false);
        TextView textview = (TextView) view.findViewById(R.id.list_header);
        textview.setText(title);
        mLinearLayout.addView(textview);
    }

    public void onCardClick(Context context) {}
	
	public void discard() {
        SharedPreferences prefs = CardManager.getContext().getSharedPreferences(DISCARD_SETTINGS, 0);
        Editor editor = prefs.edit();
        discard(editor);
        editor.commit();
    }

    protected abstract void discard(Editor editor);

    /**
     * Must be called after information has been set
     * Adds the card to CardManager if not dismissed before
     *
     * @return Returns true if it has added the card, false otherwise
     * */
    public boolean apply() {
        SharedPreferences prefs = CardManager.getContext().getSharedPreferences(DISCARD_SETTINGS, 0);
        return apply(prefs);
    }

    protected abstract boolean apply(SharedPreferences prefs);
}
