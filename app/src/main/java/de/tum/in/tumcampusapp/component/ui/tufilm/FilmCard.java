package de.tum.in.tumcampusapp.component.ui.tufilm;

import android.content.Context;
import android.content.Intent;

import de.tum.in.tumcampusapp.component.ui.news.NewsCard;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;

public class FilmCard extends NewsCard {
    public FilmCard(Context context) {
        super(CardManager.CARD_NEWS_FILM, context);
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, KinoActivity.class);
    }
}
