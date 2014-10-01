package de.tum.in.tumcampus.cards;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.adapters.NewsAdapter;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
 * Card that shows selected news
 */
public class NewsCard extends Card {

    private Cursor mCursor;
    private int mPosition;

    public NewsCard(Context context) {
        super(context, "card_news", false, false);
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_NEWS;
    }

    @Override
    protected String getTitle() {
        mCursor.moveToPosition(mPosition);
        return mCursor.getString(2);
    }

    @Override
    public View getCardView(Context context, ViewGroup parent) {
        mCursor.moveToPosition(mPosition);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View card = NewsAdapter.newNewsView(mInflater, mCursor, parent);
        NewsAdapter.bindNewsView(card, mContext, mCursor);
        return card;
    }

    /**
     * Sets the information needed to show news
     * @param c Cursor
     * @param pos Position inside the cursor
     */
    public void setNews(Cursor c, int pos) {
        mCursor = c;
        mPosition = pos;
    }

    //@Override
    //protected void discard(Editor editor) {
        //editor.putLong(NEXT_LECTURE_DATE, mDate.getTime());
    //}

    //@Override
    //protected boolean shouldShow(SharedPreferences prefs) {
        //long prevTime = prefs.getLong(NEXT_LECTURE_DATE, 0);
        //String prevTitle = prefs.getString(NEXT_LECTURE_TITLE, "");
        //return (mDate.getTime() == prevTime && !prevTitle.equals(mTitle)) || mDate.getTime() > prevTime;
    //}

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        mCursor.moveToPosition(mPosition);
        notificationBuilder.setContentTitle(mContext.getString(R.string.news));
        notificationBuilder.setContentText(mCursor.getString(2));
        /*if(Uri.parse(mLink).getHost().equals("graph.facebook.com")) {
            notificationBuilder.setContentInfo("Facebook");
        } else {
            notificationBuilder.setContentInfo(Uri.parse(mLink).getHost());
        }*/
        notificationBuilder.setTicker(mCursor.getString(2));
        Bitmap img = Utils.downloadImageToBitmap(mContext, mCursor.getString(5));
        notificationBuilder.extend(new NotificationCompat.WearableExtender().setBackground(img));
        return notificationBuilder.build();
    }

    @Override
    public Intent getIntent() {
        mCursor.moveToPosition(mPosition);
        String url = mCursor.getString(4);
        if (url.length() == 0) {
            Utils.showToast(mContext, R.string.no_link_existing);
            return null;
        }

        // Opens url in browser
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }
}
