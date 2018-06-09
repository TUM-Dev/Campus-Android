package de.tum.`in`.tumcampusapp.component.ui.overview.card

import android.content.Context

@Deprecated("No longer used")
abstract class NotificationAwareCard : Card {

    /**
     * Gets the title of the card
     */
    abstract val title: String

    constructor(cardType: Int, context: Context, settings: String) : super(cardType, context, settings) {}

    constructor(cardType: Int, context: Context, settings: String, phoneDefault: Boolean) : super(cardType, context, settings) {}

    /**
     * Determines if the card should show a notification. Decision is based on the given SharedPreferences.
     * This method should be overridden in most cases.
     *
     * @return returns true if the card should be shown
     */
    // TODO: Move somewhere else
    /*
    protected open fun shouldShowNotification(prefs: SharedPreferences): Boolean {
        return shouldShow(prefs)
    }
    */

    /**
     * Shows the card as notification if settingsPrefix allow it
     */
    /*
    private fun notifyUser() {
        // Start building our notification
        val notificationBuilder = NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notification)

        // If intent is specified add the content intent to the notification
        val intent = getIntent()
        if (intent != null) {
            val viewPendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            notificationBuilder.setContentIntent(viewPendingIntent)
        }

        // Let the card set detailed information
        val notification = fillNotification(notificationBuilder)

        if (notification != null) {
            val notificationManager = NotificationManagerCompat.from(context)
            try {
                notificationManager.notify(cardType, notification)
            } catch (e: IllegalArgumentException) { //NOPMD
                //Dismiss exception, as we want this to happen (Only work on wear)
            }

            // Showing a notification is handled as it would already be dismissed, so that it will not notify again.
            discardNotification()
        }
    }
    */

    /**
     * Should fill the given notification builder with content
     */
    /*
    protected open fun fillNotification(notificationBuilder: NotificationCompat.Builder): Notification? {
        return notificationBuilder.build()
    }
    */

    /*
    override fun getIfShowOnStart(): Card? {
        return super.getIfShowOnStart().also {
            if (mShowPhone) {
                val prefs = context.getSharedPreferences(DISCARD_SETTINGS_PHONE, 0)
                if (shouldShowNotification(prefs)) {
                    //notifyUser()
                }
            }
        }
    }
    */

    /**
     * Should be called if the notification has been dismissed
     */
    // TODO: Consider user settings for scheduling notifications
    /*
    protected fun discardNotification() {
        val prefs = context.getSharedPreferences(DISCARD_SETTINGS_PHONE, 0)
        val editor = prefs.edit()
        discardNotification(editor)
        editor.apply()
    }
    */

    /**
     * Save information about the dismissed notification to don't shown again the notification
     *
     * @param editor Editor to be used for saving values
     */
    /*
    protected open fun discardNotification(editor: SharedPreferences.Editor) {
        discard(editor)
    }
    */

}
