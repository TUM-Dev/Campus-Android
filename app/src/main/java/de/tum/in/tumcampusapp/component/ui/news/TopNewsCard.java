package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.onboarding.WizNavStartActivity;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Shows important news
 */
public class TopNewsCard extends Card {

    public TopNewsCard(Context context) {
        super(CardManager.CARD_TOP_NEWS, context, "top_news", false);
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_top_news, parent, false);

        view.findViewById(R.id.top_news_img).setOnClickListener(v -> {
            Intent loginIntent = new Intent(view.getContext(), WizNavStartActivity.class);
            view.getContext().startActivity(loginIntent);
        });

        return new CardViewHolder(view);
    }

    @Override
    public void discard(@NonNull SharedPreferences.Editor editor) {
        Utils.setSetting(this.getContext(), CardManager.SHOW_TOP_NEWS, false);
    }

    @Override
    protected boolean shouldShow(SharedPreferences p) {
        // show on top as long as user hasn't swiped it away and isn't connected to TUMonline
        return Utils.getSettingBool(this.getContext(), CardManager.SHOW_TOP_NEWS, true);
    }

    @Override
    public Intent getIntent() {
        return new Intent(this.getContext(), WizNavStartActivity.class);
    }

    @Override
    public int getId() {
        return 0;
    }
}
