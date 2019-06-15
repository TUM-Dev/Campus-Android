package de.tum.in.tumcampusapp.component.ui.tufilm;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;

import de.tum.in.tumcampusapp.component.other.navigation.NavDestination;
import de.tum.in.tumcampusapp.component.ui.news.NewsCard;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;

public class FilmCard extends NewsCard {

    public FilmCard(Context context) {
        super(CardManager.CARD_NEWS_FILM, context);
    }

    @Nullable
    @Override
    public NavDestination getNavigationDestination() {
        Bundle args = new Bundle();
        args.putString(Const.KINO_DATE, DateTimeUtils.getDateTimeString(getDate()));
        return new NavDestination.Activity(KinoActivity.class, args);
    }

}
