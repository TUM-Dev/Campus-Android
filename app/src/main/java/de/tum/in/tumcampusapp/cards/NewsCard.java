package de.tum.in.tumcampusapp.cards;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.google.common.base.Optional;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.NewsAdapter;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.cards.generic.NotificationAwareCard;
import de.tum.in.tumcampusapp.managers.CardManager;
import de.tum.in.tumcampusapp.managers.NewsManager;

/**
 * Card that shows selected news
 */
public class NewsCard extends NotificationAwareCard {

    private Cursor mCursor;
    private int mPosition;
    private final NetUtils net;

    public NewsCard(Context context) {
        this(CardManager.CARD_NEWS, context);
    }

    public NewsCard(int type, Context context) {
        super(type, context, "card_news", false, false);
        net = new NetUtils(context);
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent, int type) {
        return NewsAdapter.newNewsView(parent, type == CardManager.CARD_NEWS_FILM);
    }

    @Override
    public int getId() {
        mCursor.moveToPosition(mPosition);
        return mCursor.getInt(0);
    }

    @Override
    public String getTitle() {
        mCursor.moveToPosition(mPosition);
        return mCursor.getString(2);
    }

    public String getSource() {
        mCursor.moveToPosition(mPosition);
        return mCursor.getString(1);
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        NewsAdapter.bindNewsView(net, viewHolder, mCursor);
    }

    /**
     * Sets the information needed to show news
     *
     * @param c   Cursor
     * @param pos Position inside the cursor
     */
    public void setNews(Cursor c, int pos) {
        mCursor = c;
        mPosition = pos;
        mCursor.moveToPosition(mPosition);
    }

    @Override
    protected void discard(SharedPreferences.Editor editor) {
        NewsManager newsManager = new NewsManager(mContext);
        mCursor.moveToPosition(mPosition);
        newsManager.setDismissed(mCursor.getString(0), mCursor.getInt(9) | 1);
    }

    @Override
    protected void discardNotification(SharedPreferences.Editor editor) {
        NewsManager newsManager = new NewsManager(mContext);
        mCursor.moveToPosition(mPosition);
        newsManager.setDismissed(mCursor.getString(0), mCursor.getInt(9) | 2);
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        mCursor.moveToPosition(mPosition);
        return (mCursor.getInt(9) & 1) == 0;
    }

    @Override
    protected boolean shouldShowNotification(SharedPreferences prefs) {
        mCursor.moveToPosition(mPosition);
        return (mCursor.getInt(9) & 2) == 0;
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        mCursor.moveToPosition(mPosition);
        notificationBuilder.setContentTitle(mContext.getString(R.string.news));
        notificationBuilder.setContentText(mCursor.getString(2));
        notificationBuilder.setContentInfo(mCursor.getString(8));
        notificationBuilder.setTicker(mCursor.getString(2));
        Optional<Bitmap> img = net.downloadImageToBitmap(mCursor.getString(4));
        if (img.isPresent()) {
            notificationBuilder.extend(new NotificationCompat.WearableExtender().setBackground(img.get()));
        }
        return notificationBuilder.build();
    }

    @Override
    public Intent getIntent() {
        // Show regular news in browser
        mCursor.moveToPosition(mPosition);
        String url = mCursor.getString(3);
        if (url.isEmpty()) {
            Utils.showToast(mContext, R.string.no_link_existing);
            return null;
        }

        // Opens url in browser
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }

    @Override
    public RemoteViews getRemoteViews(Context context) {
        mCursor.moveToPosition(mPosition);
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.cards_widget_card);
        remoteViews.setTextViewText(R.id.widgetCardTextView, this.getTitle());
        final String imgURL = mCursor.getString(4);
        if (imgURL != null && !imgURL.trim()
                                     .isEmpty() && !"null".equals(imgURL)) {
            Optional<Bitmap> img = net.downloadImageToBitmap(imgURL);
            if (img.isPresent()) {
                remoteViews.setImageViewBitmap(R.id.widgetCardImageView, img.get());
            }
        }
        return remoteViews;
    }
}
