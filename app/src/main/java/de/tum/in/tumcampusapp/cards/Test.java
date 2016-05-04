package de.tum.in.tumcampusapp.cards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.CardManager;

/**
 * Created by Moh on 4/26/2016.
 */
public class Test extends Card
{
    public Test(Context context) {
        super(context);
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
        return CardManager.CARD_TEST;
    }

    @Override
    public void discard(SharedPreferences.Editor editor) {
        Utils.setSetting(mContext, CardManager.SHOW_TEST, false);
    }

    @Override
    public boolean shouldShow(SharedPreferences p) {
       return true;
    }
}
