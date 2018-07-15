package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Card that shows selected news
 */
public class NewsCard extends Card {

    protected News mNews;

    public NewsCard(Context context) {
        this(CardManager.CARD_NEWS, context);
    }

    public NewsCard(int type, Context context) {
        super(type, context, "card_news");
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent, int type) {
        return NewsAdapter.newNewsView(parent, type == CardManager.CARD_NEWS_FILM);
    }

    @Override
    public int getId() {
        return Integer.parseInt(mNews.getId());
    }

    @NonNull
    public String getTitle() {
        return mNews.getTitle();
    }

    public String getSource() {
        return mNews.getSrc();
    }

    public DateTime getDate() {
        return mNews.getDate();
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        NewsAdapter.bindNewsView(viewHolder, mNews, getContext());
    }

    /**
     * Sets the information needed to show news
     *
     * @param n   News object
     */
    public void setNews(News n) {
        mNews = n;
    }

    @Override
    protected void discard(SharedPreferences.Editor editor) {
        NewsController newsController = new NewsController(getContext());
        newsController.setDismissed(mNews.getId(), mNews.getDismissed() | 1);
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        return (mNews.getDismissed() & 1) == 0;
    }

    @Nullable
    @Override
    public Intent getIntent() {
        return mNews.getIntent(getContext());
    }

    @Override
    public RemoteViews getRemoteViews(@NonNull Context context, int appWidgetId) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.cards_widget_card);
        remoteViews.setTextViewText(R.id.widgetCardTextView, mNews.getTitle());
        final String imgURL = mNews.getImage();
        if (!imgURL.trim().isEmpty() && !"null".equals(imgURL)) {
            Utils.log(imgURL);
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(() -> {
                Picasso.get()
                        .load(imgURL)
                        .into(remoteViews, R.id.widgetCardImageView, new int[] {appWidgetId});
            });
        }
        return remoteViews;
    }

}
