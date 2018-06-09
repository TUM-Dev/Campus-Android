package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import java.util.Date;

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
        super(type, context, "card_news", false);
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent, int type) {
        return NewsAdapter.newNewsView(parent, type == CardManager.CARD_NEWS_FILM);
    }

    @Override
    public int getId() {
        return Integer.parseInt(mNews.getId());
    }

    /*
    @Override
    public String getTitle() {
        return mNews.getTitle();
    }
    */

    public String getSource() {
        return mNews.getSrc();
    }

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

    // TODO
    /*
    @Override
    protected void discardNotification(SharedPreferences.Editor editor) {
        NewsController newsController = new NewsController(getContext());
        newsController.setDismissed(mNews.getId(), mNews.getDismissed() | 2);
    }
    */

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        return (mNews.getDismissed() & 1) == 0;
    }

    // TODO
    /*
    @Override
    protected boolean shouldShowNotification(SharedPreferences prefs) {
        return (mNews.getDismissed() & 2) == 0;
    }
    */

    /*
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
    */

    @Override
    public Intent getIntent() {
        /*
        if (mNews.getLink().isEmpty()) {
            Utils.showToast(getContext(), R.string.no_link_existing);
            return null;
        }
        */

        return mNews.getIntent(getContext());
        /*
        // Show regular news in browser
        String url = mNews.getLink();
        if (url.isEmpty()) {
            Utils.showToast(getContext(), R.string.no_link_existing);
            return null;
        }

        // Opens url in browser
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        */
    }

    @Override
    public RemoteViews getRemoteViews(Context context, int appWidgetId) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.cards_widget_card);
        remoteViews.setTextViewText(R.id.widgetCardTextView, mNews.getTitle());
        final String imgURL = mNews.getImage();
        if (!imgURL.trim().isEmpty() && !"null".equals(imgURL)) {
            Utils.log(imgURL);
            Picasso.get()
                    .load(imgURL)
                    .into(remoteViews, R.id.widgetCardImageView, new int[]{appWidgetId});
        }
        return remoteViews;
    }
}
