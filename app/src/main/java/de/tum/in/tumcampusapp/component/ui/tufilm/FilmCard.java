package de.tum.in.tumcampusapp.component.ui.tufilm;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;

import de.tum.in.tumcampusapp.component.other.navigation.NavigationDestination;
import de.tum.in.tumcampusapp.component.other.navigation.SystemIntent;
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
    public NavigationDestination getNavigationDestination() {
        Intent intent = new Intent(getContext(), KinoActivity.class);
        intent.putExtra(Const.KINO_DATE, DateTimeUtils.INSTANCE.getDateTimeString(getDate()));
        return new SystemIntent(intent);
    }

}
