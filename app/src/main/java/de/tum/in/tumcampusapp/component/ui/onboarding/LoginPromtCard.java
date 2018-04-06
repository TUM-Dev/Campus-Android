package de.tum.in.tumcampusapp.component.ui.onboarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Card that prompts the user to login to TUMonline since we don't show the wizard after the first launch anymore.
 * It will be shown until it is swiped away for the first time.
 */
public class LoginPromtCard extends Card {

    public LoginPromtCard(Context context) {
        super(CardManager.CARD_LOGIN, context, null);
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.card_login_promt, parent, false);

        view.findViewById(R.id.login_button)
            .setOnClickListener(v -> {
                Intent loginIntent = new Intent(view.getContext(), WizNavStartActivity.class);
                view.getContext().startActivity(loginIntent);
            });

        return new Card.CardViewHolder(view);
    }

    @Override
    public void discard(SharedPreferences.Editor editor) {
        Utils.setSetting(mContext, CardManager.SHOW_LOGIN, false);
    }

    @Override
    protected boolean shouldShow(SharedPreferences p) {
        // show on top as long as user hasn't swiped it away and isn't connected to TUMonline
        return Utils.getSettingBool(mContext, CardManager.SHOW_LOGIN, true)
               && Utils.getSetting(mContext, Const.LRZ_ID, "").isEmpty();
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, WizNavStartActivity.class);
    }

    @Override
    public int getId() {
        return 0;
    }
}
