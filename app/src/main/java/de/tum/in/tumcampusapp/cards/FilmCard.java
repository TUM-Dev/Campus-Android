package de.tum.in.tumcampusapp.cards;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import de.tum.in.tumcampusapp.activities.KinoActivity;
import de.tum.in.tumcampusapp.models.managers.CardManager;

public class FilmCard extends NewsCard {
    public FilmCard(Context context) {
        super(CardManager.CARD_NEWS_FILM, context);
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, KinoActivity.class);
    }

    public static boolean isNewsAFilm(Cursor c, int pos) {
        c.moveToPosition(pos);
        return c.getInt(1) == 2;
    }
}
