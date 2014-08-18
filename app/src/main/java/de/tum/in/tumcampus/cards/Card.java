package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tum.in.tumcampus.R;

/**
* Created by Florian on 17.08.2014.
*/
public abstract class Card {
    protected TextView mTitleView;
    protected TextView mDateView;
    protected View mCard;
    protected LinearLayout mLinearLayout;
    protected LayoutInflater mInflater;

    public abstract int getTyp();
    public View getView(Context context, ViewGroup parent) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCard = mInflater.inflate(R.layout.card_item, parent, false);
        mLinearLayout = (LinearLayout) mCard.findViewById(R.id.card_view);
        mTitleView = (TextView) mCard.findViewById(R.id.card_title);
        mDateView = (TextView) mCard.findViewById(R.id.card_date);
        return mCard;
    }

    public void addTextView(Context context, String text) {
        float density = context.getResources().getDisplayMetrics().density;
        TextView textview = new TextView(context);
        textview.setText(text);
        textview.setPadding((int)(10*density), 0, (int)(10*density), 0);
        mLinearLayout.addView(textview);
    }

    protected void addHeader(Context context, String title) {
        View view = mInflater.inflate(R.layout.list_header, mLinearLayout, false);
        TextView textview = (TextView) view.findViewById(R.id.list_header);
        textview.setText(title);
        mLinearLayout.addView(textview);
    }
/*
    protected void addButton(String text, View.OnClickListener listener) {
        View view = mInflater.inflate(R.layout.card_button, mLinearLayout, false);
        Button but1 = (Button) view.findViewById(R.id.card_button1);
        but1.setText(text);
        but1.setOnClickListener(listener);
        mLinearLayout.addView(view);
    }*/

    public void onCardClick(Context context) {}
}
