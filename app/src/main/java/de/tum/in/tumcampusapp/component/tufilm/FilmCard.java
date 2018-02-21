package de.tum.in.tumcampusapp.component.tufilm;

import android.content.Context;
import android.content.Intent;

import de.tum.in.tumcampusapp.component.news.NewsCard;
import de.tum.in.tumcampusapp.component.overview.CardManager;

public class FilmCard extends NewsCard {
    public FilmCard(Context context) {
        super(CardManager.CARD_NEWS_FILM, context);
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, KinoActivity.class);
    }
}
