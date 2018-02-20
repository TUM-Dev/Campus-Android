package de.tum.in.tumcampusapp.cards.generic;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;

public abstract class NotificationAwareCard extends Card {
    public NotificationAwareCard(int cardType, Context context, String settings) {
        super(cardType, context, settings);
    }

    public NotificationAwareCard(int cardType, Context context, String settings, boolean wearDefault, boolean phoneDefault) {
        super(cardType, context, settings, wearDefault, phoneDefault);
    }

    /**
     * Determines if the card should show a notification. Decision is based on the given SharedPreferences.
     * This method should be overridden in most cases.
     *
     * @return returns true if the card should be shown
     */
    protected boolean shouldShowNotification(SharedPreferences prefs) {
        return shouldShow(prefs);
    }

    /**
     * Shows the card as notification if settings allow it
     */
    private void notifyUser() {
        // Start building our notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext, Const.NOTIFICATION_CHANNEL_DEFAULT)
                        .setAutoCancel(true)
                        .setContentTitle(getTitle());

        // If intent is specified add the content intent to the notification
        final Intent intent = getIntent();
        if (intent != null) {
            PendingIntent viewPendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            notificationBuilder.setContentIntent(viewPendingIntent);
        }

        // Apply trick to hide card on phone if it the notification
        // should only be present on the watch
        if (mShowWear && !mShowPhone) {
            notificationBuilder.setGroup("GROUP_" + getType());
            notificationBuilder.setGroupSummary(false);
        } else {
            notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        }

        // Let the card set detailed information
        Notification notification = fillNotification(notificationBuilder);

        if (notification != null) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
            try {
                notificationManager.notify(getType(), notification);
            } catch (IllegalArgumentException e) { //NOPMD
                //Dismiss exception, as we want this to happen (Only work on wear)
            }
            // Showing a notification is handled as it would already be dismissed, so that it will not notify again.
            discardNotification();
        }
    }

    /**
     * Should fill the given notification builder with content
     */
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        return notificationBuilder.build();
    }

    /**
     * Gets the title of the card
     */
    public abstract String getTitle();

    @Override
    public void apply() {
        super.apply();
        // Should be shown on phone or watch?
        if (mShowWear || mShowPhone) {
            SharedPreferences prefs = mContext.getSharedPreferences(DISCARD_SETTINGS_PHONE, 0);
            if (shouldShowNotification(prefs)) {
                notifyUser();
            }
        }
    }

    /**
     * Should be called if the notification has been dismissed
     */
    protected void discardNotification() {
        SharedPreferences prefs = mContext.getSharedPreferences(DISCARD_SETTINGS_PHONE, 0);
        SharedPreferences.Editor editor = prefs.edit();
        discardNotification(editor);
        editor.apply();
    }

    /**
     * Save information about the dismissed notification to don't shown again the notification
     *
     * @param editor Editor to be used for saving values
     */
    protected void discardNotification(SharedPreferences.Editor editor) {
        discard(editor);
    }
}
