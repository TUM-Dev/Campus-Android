package de.tum.in.tumcampus.cards;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.MultiSelectListPreference;
import de.tum.in.tumcampus.models.managers.CardManager;

public abstract class Card {
    public static final String DISCARD_SETTINGS_START = "discard_settings_start";
    private static final String DISCARD_SETTINGS_PHONE = "discard_settings_phone";
    private static final String defaultVal = "1\u0001\u0007\u001D\u0007\u00013";
    
    // Context related stuff
    protected Context mContext;
    protected LayoutInflater mInflater;
    
    // UI Elements
    protected View mCard;
    protected LinearLayout mLinearLayout;
    protected TextView mTitleView;
    protected TextView mDateView;
    
    // Settings for showing this card on startpage or as notification
    private boolean mShowStart = true;
    private boolean mShowPhone = false;
    private boolean mShowWear = false;

    public Card(Context context, String settings) {
        this(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        List<String> show = MultiSelectListPreference.getFromString(prefs.getString(settings, defaultVal));
        mShowStart = show.contains("1");
        mShowPhone = show.contains("2");
        mShowWear = show.contains("3");
    }

    public Card(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public abstract int getTyp();

    public View getCardView(Context context, ViewGroup parent) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCard = mInflater.inflate(R.layout.card_item, parent, false);
        mLinearLayout = (LinearLayout) mCard.findViewById(R.id.card_view);
        mTitleView = (TextView) mCard.findViewById(R.id.card_title);
        mTitleView.setText(getTitle());
        mDateView = (TextView) mCard.findViewById(R.id.card_date);
        return mCard;
    }

    public TextView addTextView(String text) {
        TextView textview = new TextView(mContext);
        textview.setText(text);

        //Give some space to the other stuff on the card
        int padding = (int) mContext.getResources().getDimension(R.dimen.card_text_padding);
        textview.setPadding(padding, 0, padding, 0);

        mLinearLayout.addView(textview);
        return textview;
    }

    protected void addHeader(String title) {
        View view = mInflater.inflate(R.layout.card_list_header, mLinearLayout, false);
        TextView textview = (TextView) view.findViewById(R.id.list_header);
        textview.setText(title);
        mLinearLayout.addView(textview);
    }

    public void onCardClick() {
        Intent i = getIntent();
        if(i!=null)
            mContext.startActivity(i);
    }

    /**
     * Should be called after the user has dismissed the card
     * */
	public void discardCard() {
        SharedPreferences prefs = CardManager.getContext().getSharedPreferences(DISCARD_SETTINGS_START, 0);
        Editor editor = prefs.edit();
        discard(editor);
        editor.commit();
    }

    /**
     * Should be called if the notification has been dismissed
     * */
    public void discardNotification() {
        SharedPreferences prefs = CardManager.getContext().getSharedPreferences(DISCARD_SETTINGS_PHONE, 0);
        Editor editor = prefs.edit();
        discard(editor);
        editor.commit();
    }

    /**
     * Save information about the dismissed card/notification to decide later if the card should be shown again
     * */
    protected void discard(Editor editor) {}

    /**
     * Must be called after information has been set
     * Adds the card to CardManager if not dismissed before and notifies the user
     * */
    public void apply() {
        // Should be shown on start page?
        boolean show = false;
        if(mShowStart) {
            SharedPreferences prefs = CardManager.getContext().getSharedPreferences(DISCARD_SETTINGS_START, 0);
            show = shouldShow(prefs);
        }
        CardManager.addCard(this, show);

        // Should be shown on phone or watch?
        if(mShowPhone||mShowWear) {
            SharedPreferences prefs = CardManager.getContext().getSharedPreferences(DISCARD_SETTINGS_PHONE, 0);
            if (shouldShow(prefs))
                notifyUser();
        }
    }

    /**
     * Determines if the card should be shown. Decision is based on the given SharedPreferences
     * @return returns true if the card should be shown
     * */
    protected boolean shouldShow(SharedPreferences prefs) {
        return true;
    }

    /**
     * Shows the card as notification if settings allow it
     * */
    public void notifyUser() {
        if(!mShowPhone && !mShowWear)
            return;

        Intent viewIntent = new Intent(mContext, CardManager.DismissHandler.class);
        viewIntent.putExtra(CardManager.NOTIFICATION_ID, getTyp());
        viewIntent.putExtra(CardManager.SHOW_CONTENT, true);

        PendingIntent viewPendingIntent = PendingIntent.getService(mContext, getTyp(), viewIntent, 0);

        Intent dismissIntent = new Intent(mContext, CardManager.DismissHandler.class);
        dismissIntent.putExtra(CardManager.NOTIFICATION_ID, getTyp());

        PendingIntent dismissPendingIntent = PendingIntent.getService(mContext, getTyp(), dismissIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.tum_logo)
                        .setContentIntent(viewPendingIntent)
                        .setContentTitle(getTitle())
                        .setDeleteIntent(dismissPendingIntent);

        // Trick to hide card on phone
        if(!mShowPhone) {
            notificationBuilder.setGroup("GROUP_" + getTyp());
            notificationBuilder.setGroupSummary(false);
        }

        // Let the card set detailed information
        Notification notification = fillNotification(notificationBuilder);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(mContext);
        notificationManager.notify(getTyp(), notification);
    }

    /**
     * Should fill the given notification builder with content
     * */
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        return notificationBuilder.build();
    }

    /**
     * @return Should return the intent that should be launched if the card
     * or the notification gets clicked, null if nothing should happen
     * */
    public Intent getIntent() {
        return null;
    }

    /**
     * Gets the title of the card
     * */
    protected String getTitle() {
        return null;
    }

    /**
     * Tells the list adapter and indirectly the
     * SwipeDismissList if the item is dismissable
     * The restore card is not dismissable.
     * */
    public boolean isDismissable() {
        return true;
    }

}
