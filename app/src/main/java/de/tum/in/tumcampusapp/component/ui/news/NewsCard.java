package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
<<<<<<< HEAD
||||||| merged common ancestors
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
=======
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
>>>>>>> f4f8898a1a23136ceb8aa963953acf18c26f2eed
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

<<<<<<< HEAD
import java.util.Date;
||||||| merged common ancestors
import java.io.IOException;
import java.util.Date;
=======
import org.joda.time.DateTime;

import java.io.IOException;
>>>>>>> f4f8898a1a23136ceb8aa963953acf18c26f2eed

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

<<<<<<< HEAD
||||||| merged common ancestors
    @Override
    public String getTitle() {
        return mNews.getTitle();
    }

=======
    @NonNull
    @Override
    public String getTitle() {
        return mNews.getTitle();
    }

>>>>>>> f4f8898a1a23136ceb8aa963953acf18c26f2eed
    public String getSource() {
        return mNews.getSrc();
    }

<<<<<<< HEAD
    public Date getDate() {
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

||||||| merged common ancestors
    public Date getDate() {
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
    protected void discardNotification(SharedPreferences.Editor editor) {
        NewsController newsController = new NewsController(getContext());
        newsController.setDismissed(mNews.getId(), mNews.getDismissed() | 2);
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        return (mNews.getDismissed() & 1) == 0;
    }

=======
>>>>>>> f4f8898a1a23136ceb8aa963953acf18c26f2eed
    @Override
<<<<<<< HEAD
||||||| merged common ancestors
    protected boolean shouldShowNotification(SharedPreferences prefs) {
        return (mNews.getDismissed() & 2) == 0;
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        NewsSourcesDao newsSourcesDao = TcaDb.getInstance(getContext()).newsSourcesDao();
        NewsSources newsSource = newsSourcesDao.getNewsSource(Integer.parseInt(mNews.getSrc()));
        notificationBuilder.setContentTitle(getContext().getString(R.string.news));
        notificationBuilder.setContentText(mNews.getTitle());
        notificationBuilder.setContentInfo(newsSource.getTitle());
        notificationBuilder.setTicker(mNews.getTitle());
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        try {
            if(!mNews.getImage().isEmpty()){
                Bitmap bgImg = Picasso.get().load(mNews.getImage()).get();
                notificationBuilder.extend(new NotificationCompat.WearableExtender().setBackground(bgImg));
            }
        } catch (IOException e) {
            // ignore it if download fails
        }
        return notificationBuilder.build();
    }

    @Override
=======
    protected boolean shouldShowNotification(@NonNull SharedPreferences prefs) {
        return (mNews.getDismissed() & 2) == 0;
    }

    @Override
    protected Notification fillNotification(@NonNull NotificationCompat.Builder notificationBuilder) {
        NewsSourcesDao newsSourcesDao = TcaDb.getInstance(getContext()).newsSourcesDao();
        NewsSources newsSource = newsSourcesDao.getNewsSource(Integer.parseInt(mNews.getSrc()));
        notificationBuilder.setContentTitle(getContext().getString(R.string.news));
        notificationBuilder.setContentText(mNews.getTitle());
        notificationBuilder.setContentInfo(newsSource.getTitle());
        notificationBuilder.setTicker(mNews.getTitle());
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        try {
            if(!mNews.getImage().isEmpty()){
                Bitmap bgImg = Picasso.get().load(mNews.getImage()).get();
                notificationBuilder.extend(new NotificationCompat.WearableExtender().setBackground(bgImg));
            }
        } catch (IOException e) {
            // ignore it if download fails
        }
        return notificationBuilder.build();
    }

    /**
     * Sets the information needed to show news
     *
     * @param n News object
     */
    public void setNews(News n) {
        mNews = n;
    }

    @Override
    protected void discardNotification(@NonNull SharedPreferences.Editor editor) {
        NewsController newsController = new NewsController(getContext());
        newsController.setDismissed(mNews.getId(), mNews.getDismissed() | 2);
    }

    public DateTime getDate() {
        return mNews.getDate();
    }

    @Nullable
    @Override
>>>>>>> f4f8898a1a23136ceb8aa963953acf18c26f2eed
    public Intent getIntent() {
        return mNews.getIntent(getContext());
    }

    @Override
    public void updateViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        NewsAdapter.bindNewsView(viewHolder, mNews, getContext());
    }

    @Override
    protected boolean shouldShow(@NonNull SharedPreferences prefs) {
        return (mNews.getDismissed() & 1) == 0;
    }

    @Override
    public RemoteViews getRemoteViews(@NonNull Context context, int appWidgetId) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.cards_widget_card);
        remoteViews.setTextViewText(R.id.widgetCardTextView, mNews.getTitle());
        final String imgURL = mNews.getImage();
        if (!imgURL.trim().isEmpty() && !"null".equals(imgURL)) {
            Utils.log(imgURL);
            Picasso.get()
                   .load(imgURL)
                   .into(remoteViews, R.id.widgetCardImageView, new int[]{appWidgetId});

            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(() -> {
                Picasso.get()
                        .load(imgURL)
                        .into(remoteViews, R.id.widgetCardImageView, new int[] {appWidgetId});
            });
        }
        return remoteViews;
    }

    @Override
    protected void discard(@NonNull SharedPreferences.Editor editor) {
        NewsController newsController = new NewsController(getContext());
        newsController.setDismissed(mNews.getId(), mNews.getDismissed() | 1);
    }
}
