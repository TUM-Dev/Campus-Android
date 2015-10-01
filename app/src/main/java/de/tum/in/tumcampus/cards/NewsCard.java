package de.tum.in.tumcampus.cards;

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

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.KinoActivity;
import de.tum.in.tumcampus.adapters.NewsAdapter;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.NewsManager;

/**
 * Card that shows selected news
 */
public class NewsCard extends Card {

    private Cursor mCursor;
    private int mPosition;
    private NetUtils net;
    private boolean isFilm = false;

    public NewsCard(Context context) {
        super(context, "card_news", false, false);
        net = new NetUtils(context);
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent, int type) {
        return NewsAdapter.newNewsView(parent, type == CardManager.CARD_NEWS_FILM);
    }

    @Override
    public int getTyp() {
        return isFilm ? CardManager.CARD_NEWS_FILM : CardManager.CARD_NEWS;
    }

    @Override
    public int getId() {
        mCursor.moveToPosition(mPosition);
        return mCursor.getInt(0);
    }

    @Override
    protected String getTitle() {
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
        this.isFilm = mCursor.getInt(1) == 2;
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
    boolean shouldShow(SharedPreferences prefs) {
        mCursor.moveToPosition(mPosition);
        return (mCursor.getInt(9) & 1) == 0;
    }

    @Override
    boolean shouldShowNotification(SharedPreferences prefs) {
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
        Bitmap img = net.downloadImageToBitmap(mCursor.getString(4));
        notificationBuilder.extend(new NotificationCompat.WearableExtender().setBackground(img));
        return notificationBuilder.build();
    }

    @Override
    public Intent getIntent() {
        if (isFilm) {
            return new Intent(mContext, KinoActivity.class);
        } else {
            // Show regular news in browser
            mCursor.moveToPosition(mPosition);
            String url = mCursor.getString(3);
            if (url.length() == 0) {
                Utils.showToast(mContext, R.string.no_link_existing);
                return null;
            }

            // Opens url in browser
            return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }
    }
}
