package de.tum.in.tumcampusapp.component.ui.tufilm;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import de.tum.in.tumcampusapp.component.other.navigation.NavigationDestination;
import de.tum.in.tumcampusapp.component.other.navigation.SystemIntent;
import de.tum.in.tumcampusapp.component.ui.news.NewsCard;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;

public class FilmCard extends NewsCard {

    public FilmCard(Context context) {
        super(CardManager.CARD_NEWS_FILM, context);
    }

    @Nullable
    @Override
<<<<<<< HEAD
    public NavigationDestination getNavigationDestination() {
        Intent intent = new Intent(getContext(), KinoActivity.class);
        intent.putExtra(Const.KINO_DATE, DateTimeUtils.INSTANCE.getDateTimeString(getDate()));
        return new SystemIntent(intent);
||||||| merged common ancestors
    public Intent getIntent() {
        Intent intent = new Intent(getContext(), KinoActivity.class);
        intent.putExtra(Const.KINO_DATE, DateTimeUtils.INSTANCE.getDateTimeString(getDate()));
        return intent;
=======
    public Intent getIntent() {
        return mNews.getIntent(getContext());
>>>>>>> d027d24d2097be437522a05f7e6abc7c2a5e863e
    }

}
