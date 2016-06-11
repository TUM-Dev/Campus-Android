package de.tum.in.tumcampusapp.cards;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.models.managers.CardManager;

/**
 * Card that describes how to dismiss a card
 */
public class IkomCard extends Card {

    public IkomCard(Context context) {
        super(context);
    }

    public static CardViewHolder inflateViewHolder(final ViewGroup parent) {
        JodaTimeAndroid.init(parent.getContext());
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_ikom, parent, false);

        //Check if we should switch the Image on certain days
        ImageView image = (ImageView) view.findViewById(R.id.imageIkom);
        DateTime firstDay = new DateTime(2016, 6, 20, 0, 0);//Display for the full first day
        DateTime thirdDay = new DateTime(2016, 6, 22, 0, 0);//And for the third and fourth
        DateTime today = new DateTime();
        if ((today.isAfter(firstDay) && today.isBefore(firstDay.plusDays(1))) ||
                (today.isAfter(thirdDay) && today.isBefore(thirdDay.plusDays(2)))) {
            image.setImageResource(R.drawable.ikom_2);
        }

        //Add a listener to the button
        Button bttn = (Button) view.findViewById(R.id.bttnIkom);
        bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ikom.tum.de"));
                    parent.getContext().startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        return new CardViewHolder(view);
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_IKOM;
    }

    @Override
    public void discard(Editor editor) {
        SharedPreferences prefs = mContext.getSharedPreferences(Card.DISCARD_SETTINGS_START, 0);
        prefs.edit().putBoolean(CardManager.SHOW_IKOM, false).apply();
    }

    @Override
    public boolean shouldShow(SharedPreferences p) {
        //Either use day: display always when event is running
        JodaTimeAndroid.init(mContext);
        DateTime firstDay = new DateTime(2016, 6, 18, 0, 0);
        DateTime today = new DateTime();

        //Or discarded manually
        SharedPreferences prefs = mContext.getSharedPreferences(Card.DISCARD_SETTINGS_START, 0);
        return prefs.getBoolean(CardManager.SHOW_IKOM, true) || (today.isAfter(firstDay) && today.isBefore(firstDay.plusDays(7)));
    }
}
