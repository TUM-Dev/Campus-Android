package de.tum.in.tumcampusapp.cards;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.UserPreferencesActivity;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.ImplicitCounter;
import de.tum.in.tumcampusapp.models.managers.CardManager;

/**
 * Base class for all cards
 */
public abstract class Card {
    public static final String DISCARD_SETTINGS_START = "discard_settings_start";
    private static final String DISCARD_SETTINGS_PHONE = "discard_settings_phone";

    // Context related stuff
    Context mContext;
    // UI Elements
    View mCard;
    LinearLayout mLinearLayout;
    TextView mTitleView;
    private String mSettings;
    // Settings for showing this card on start page or as notification
    // Default values set for restore card, no internet card, etc.
    private boolean mShowStart = true;
    private boolean mShowWear = false;
    private boolean mShowPhone = false;

    /**
     * Card constructor for special cards that don't have a preference screen
     *
     * @param context Context
     */
    Card(Context context) {
        mSettings = null;
        mContext = context;
    }

    /**
     * Standard card constructor.
     *
     * @param context  Context
     * @param settings Preference key prefix used for all preferences belonging to that card
     */
    Card(Context context, String settings) {
        this(context, settings, true, false);
        mSettings = settings;
    }

    /**
     * Card constructor that can set special default values for wear and phone
     *
     * @param context      Context
     * @param settings     Preference key prefix used for all preferences belonging to that card
     * @param wearDefault  True if notifications should by default be displayed on android wear
     * @param phoneDefault True if notifications should by default be displayed on the phone
     */
    Card(Context context, String settings, boolean wearDefault, boolean phoneDefault) {
        this(context);
        mSettings = settings;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mShowStart = prefs.getBoolean(settings + "_start", true);
        mShowWear = prefs.getBoolean(settings + "_wear", wearDefault);
        mShowPhone = prefs.getBoolean(settings + "_phone", phoneDefault);
    }

    /**
     * Gets the card typ
     *
     * @return Returns an individual integer for each card typ
     */
    public abstract int getTyp();

    /**
     * Updates the Cards content.
     * Override this method, if the card contains any dynamic content, that is not already in its XML
     *
     * @param viewHolder The Card specific view holder
     */
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        mContext = viewHolder.itemView.getContext();
        ImplicitCounter.CounterCard(mContext, this);
    }

    /**
     * Adds a new text view to the main card layout
     *
     * @param text Text that should be shown
     * @return Handle to the {@link android.widget.TextView}
     */
    TextView addTextView(String text) {
        TextView textview = new TextView(mContext);
        textview.setText(text);

        //Give some space to the other stuff on the card
        int padding = (int) mContext.getResources().getDimension(R.dimen.card_text_padding);
        textview.setPadding(padding, 0, padding, 0);

        mLinearLayout.addView(textview);
        return textview;
    }

    /**
     * Should be called after the user has dismissed the card
     */
    public void discardCard() {
        SharedPreferences prefs = mContext.getSharedPreferences(DISCARD_SETTINGS_START, 0);
        Editor editor = prefs.edit();
        discard(editor);
        editor.apply();
    }

    /**
     * Should be called if the notification has been dismissed
     */
    private void discardNotification() {
        SharedPreferences prefs = mContext.getSharedPreferences(DISCARD_SETTINGS_PHONE, 0);
        Editor editor = prefs.edit();
        discardNotification(editor);
        editor.apply();
    }

    /**
     * Save information about the dismissed card/notification to decide later if the card should be shown again
     *
     * @param editor Editor to be used for saving values
     */
    void discard(Editor editor) {
    }

    /**
     * Save information about the dismissed notification to don't shown again the notification
     *
     * @param editor Editor to be used for saving values
     */
    void discardNotification(Editor editor) {
        discard(editor);
    }

    /**
     * Must be called after information has been set
     * Adds the card to CardManager if not dismissed before and notifies the user
     */
    public void apply() {
        // Should be shown on start page?
        if (mShowStart) {
            SharedPreferences prefs = mContext.getSharedPreferences(DISCARD_SETTINGS_START, 0);
            if (shouldShow(prefs))
                CardManager.addCard(this);
        }

        // Should be shown on phone or watch?
        if (mShowWear || mShowPhone) {
            SharedPreferences prefs = mContext.getSharedPreferences(DISCARD_SETTINGS_PHONE, 0);
            if (shouldShowNotification(prefs))
                notifyUser();
        }
    }

    /**
     * Determines if the card should be shown. Decision is based on the given SharedPreferences.
     * This method should be overridden in most cases.
     *
     * @return returns true if the card should be shown
     */
    boolean shouldShow(SharedPreferences prefs) {
        return true;
    }

    /**
     * Determines if the card should be shown. Decision is based on the given SharedPreferences.
     * This method should be overridden in most cases.
     *
     * @return returns true if the card should be shown
     */
    boolean shouldShowNotification(SharedPreferences prefs) {
        return shouldShow(prefs);
    }

    /**
     * Shows the card as notification if settings allow it
     */
    private void notifyUser() {
        // Start building our notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext)
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
            notificationBuilder.setGroup("GROUP_" + getTyp());
            notificationBuilder.setGroupSummary(false);
        } else {
            notificationBuilder.setSmallIcon(R.drawable.tum_logo);
        }

        // Let the card set detailed information
        Notification notification = fillNotification(notificationBuilder);

        if (notification != null) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
            try {
                notificationManager.notify(getTyp(), notification);
            } catch (IllegalArgumentException e) {
                //Dismiss exception, as we want this to happen (Only work on wear)
            }
            // Showing a notification is handled as it would already be dismissed, so that it will not notify again.
            discardNotification();
        }
    }

    /**
     * Should fill the given notification builder with content
     */
    Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        return notificationBuilder.build();
    }

    /**
     * @return Should return the intent that should be launched if the card
     * or the notification gets clicked, null if nothing should happen
     */
    public Intent getIntent() {
        return null;
    }

    /**
     * Gets the title of the card
     */
    String getTitle() {
        return null;
    }

    /**
     * Tells the list adapter and indirectly the
     * SwipeDismissList if the item is dismissable
     * The restore card is not dismissable.
     */
    public boolean isDismissable() {
        return true;
    }

    /**
     * Gets the prefix that is used for all preference key's belonging to that card
     *
     * @return Key prefix e.g. "card_cafeteria"
     */
    public String getSettings() {
        return mSettings;
    }

    /**
     * Sets preferences so that this card does not show up again until
     * reactivated manually by the user
     */
    public void hideAlways() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Editor e = prefs.edit();
        e.putBoolean(mSettings + "_start", false);
        e.putBoolean(mSettings + "_wear", false);
        e.putBoolean(mSettings + "_phone", false);
        e.apply();
    }

    public int getId() {
        return 0;
    }

    /**
     * Interface which has to be implemented by a manager class to add cards to the stream
     */
    public interface ProvidesCard {
        /**
         * Gets called whenever cards need to be shown or refreshed.
         * This method should decide whether a card can be displayed and if so
         * call {@link Card#apply()} to tell the card manager.
         *
         * @param context Context
         */
        void onRequestCard(Context context);
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {
        private Card current;
        private List<View> addedViews = new ArrayList<>();
        private final Activity mActivity;

        public CardViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mActivity = (Activity) itemView.getContext();
        }

        public Card getCurrentCard() {
            return current;
        }

        public void setCurrentCard(Card current) {
            this.current = current;
        }

        public List<View> getAddedViews() {
            return addedViews;
        }

        public void setAddedViews(List<View> addedViews) {
            this.addedViews = addedViews;
        }

        @Override
        public void onClick(View v) {
            Intent i = current.getIntent();
            String transitionName = mActivity.getString(R.string.transition_card);
            if (i != null) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        mActivity, v, transitionName
                );
                ActivityCompat.startActivity(mActivity, i, options.toBundle());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            String key = current.getSettings();
            if (key == null) {
                return false;
            }
            PopupMenu menu = new PopupMenu(v.getContext(), v, Gravity.CENTER_HORIZONTAL);
            MenuInflater inf = menu.getMenuInflater();
            inf.inflate(R.menu.card_popup_menu, menu.getMenu());

            menu.show();
            return true;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int i = item.getItemId();
            if (i == R.id.open_card_setting) {// Open card's preference screen
                String key = current.getSettings();
                if (key == null)
                    return true;
                Intent intent = new Intent(itemView.getContext(), UserPreferencesActivity.class);
                intent.putExtra(Const.PREFERENCE_SCREEN, key);
                itemView.getContext().startActivity(intent);
                return true;
            } else if (i == R.id.always_hide_card) {
                current.hideAlways();
                current.discardCard();
                return true;
            } else {
                return false;
            }
        }
    }
}
