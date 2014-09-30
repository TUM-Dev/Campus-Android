package de.tum.in.tumcampus.cards;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.NewsActivity;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
 * Card that shows selected news
 */
public class NewsCard extends Card {

    private String mTitle;
    private String mDate;
    private String mLink;
    private String mImage;

    public NewsCard(Context context) {
        super(context, "card_news", false, false);
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_NEWS;
    }

    @Override
    protected String getTitle() {
        return mTitle;
    }

    @Override
    public View getCardView(Context context, ViewGroup parent) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCard = mInflater.inflate(R.layout.card_news_item, parent, false);
        ImageView imageView = (ImageView) mCard.findViewById(R.id.news_img);
        mTitleView = (TextView) mCard.findViewById(R.id.news_title);
        mTitleView.setText(getTitle());
        mDateView = (TextView) mCard.findViewById(R.id.news_src_date);
        TextView srcTitleView = (TextView) mCard.findViewById(R.id.news_src_title);
        ImageView srcIconView = (ImageView) mCard.findViewById(R.id.news_src_icon);

        if(mImage.isEmpty()) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            Utils.loadAndSetImage(mContext, mImage, imageView);
        }

        if (mLink.length() > 0) {
            if(Uri.parse(mLink).getHost().equals("graph.facebook.com")) {
                srcTitleView.setText("Facebook");
                srcIconView.setImageResource(R.drawable.ic_facebook);
            } else {
                srcTitleView.setText(Uri.parse(mLink).getHost());
                srcIconView.setImageResource(R.drawable.ic_comment);
            }
        }

        mDateView.setText(mDate);
        return mCard;
    }

    /**
     * Sets the information needed to show news
     * @param img Big image
     * @param title Title
     * @param link Url
     * @param date Date
     */
    public void setNews(String img, String title, String link, String date) {
        mImage = img;
        mTitle = title;
        mDate = date;
        mLink = link;
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
        notificationBuilder.setContentTitle(mContext.getString(R.string.news));
        notificationBuilder.setContentText(mTitle);
        if(Uri.parse(mLink).getHost().equals("graph.facebook.com")) {
            notificationBuilder.setContentInfo("Facebook");
        } else {
            notificationBuilder.setContentInfo(Uri.parse(mLink).getHost());
        }
        notificationBuilder.setTicker(mTitle);
        Bitmap img = Utils.downloadImageToBitmap(mContext, mImage);
        notificationBuilder.extend(new NotificationCompat.WearableExtender().setBackground(img));
        return notificationBuilder.build();
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, NewsActivity.class);
    }
}
