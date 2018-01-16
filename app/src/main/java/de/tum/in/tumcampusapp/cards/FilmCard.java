package de.tum.in.tumcampusapp.cards;

import android.content.Context;
import android.content.Intent;

import de.tum.in.tumcampusapp.activities.KinoActivity;
import de.tum.in.tumcampusapp.managers.CardManager;
import de.tum.in.tumcampusapp.models.tumcabe.News;

public class FilmCard extends NewsCard {
    public FilmCard(Context context) {
        super(CardManager.CARD_NEWS_FILM, context);
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, KinoActivity.class);
    }
}
