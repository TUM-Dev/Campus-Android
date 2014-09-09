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

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.managers.CardManager;

public abstract class Card {
    public static final String DISCARD_SETTINGS_START = "discard_settings_start";
    public static final String DISCARD_SETTINGS_PHONE = "discard_settings_phone";
    
    // Context related stuff
    protected Context mContext;
    protected LayoutInflater mInflater;
    
    // UI Elements
    protected View mCard;
    protected LinearLayout mLinearLayout;
    protected TextView mTitleView;
    protected TextView mDateView;
    
    // Settings for showing this card on startpage or as notification
    // Default values set for restore card, no internet card, etc.
    private boolean mShowStart = true;
    private boolean mShowWear = false;
    private boolean mShowPhone = false;

    public Card(Context context, String settings) {
        this(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mShowStart = prefs.getBoolean(settings+"_start", true);
        mShowWear = prefs.getBoolean(settings+"_wear", true);
        mShowPhone = prefs.getBoolean(settings+"_phone", false);
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

    /**
     * Should be called after the user has dismissed the card
     * */
	public void discardCard() {
        SharedPreferences prefs = CardManager.getContext().getSharedPreferences(DISCARD_SETTINGS_START, 0);
        Editor editor = prefs.edit();
        discard(editor);
        editor.apply();
    }

    /**
     * Should be called if the notification has been dismissed
     * */
    private void discardNotification() {
        SharedPreferences prefs = CardManager.getContext().getSharedPreferences(DISCARD_SETTINGS_PHONE, 0);
        Editor editor = prefs.edit();
        discard(editor);
        editor.apply();
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
        if(mShowStart) {
            SharedPreferences prefs = CardManager.getContext().getSharedPreferences(DISCARD_SETTINGS_START, 0);
            if(shouldShow(prefs))
                CardManager.addCard(this);
        }

        // Should be shown on phone or watch?
        if(mShowWear || mShowPhone) {
            SharedPreferences prefs = CardManager.getContext().getSharedPreferences(DISCARD_SETTINGS_PHONE, 0);
            if (shouldShow(prefs))
                notifyUser();
        }
    }

    /**
     * Determines if the card should be shown. Decision is based on the given SharedPreferences.
     * This method should be overridden in most cases.
     *
     * @return returns true if the card should be shown
     * */
    protected boolean shouldShow(SharedPreferences prefs) {
        return true;
    }

    /**
     * Shows the card as notification if settings allow it
     * */
    private void notifyUser() {
        // Showing a notification is handled as it would already be dismissed, so that it will not
        // notify again.
        discardNotification();

        // Start building our notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext)
                        .setAutoCancel(true)
                        .setContentTitle(getTitle());

        // If intent is specified add the content intent to the notification
        final Intent intent = getIntent();
        if(intent!=null) {
            PendingIntent viewPendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            notificationBuilder.setContentIntent(viewPendingIntent);
        }

        // Apply trick to hide card on phone if it the notification
        // should only be present on the watch
        if(mShowWear && !mShowPhone) {
            notificationBuilder.setGroup("GROUP_" + getTyp());
            notificationBuilder.setGroupSummary(false);
        } else {
            notificationBuilder.setSmallIcon(R.drawable.tum_logo_notification);
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

    public static interface ProvidesCard {
        public void onRequestCard(Context context) throws Exception;
    }
}
