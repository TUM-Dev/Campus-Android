package de.tum.in.tumcampusapp.component.ui.overview.card;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.reporting.stats.ImplicitCounter;
import de.tum.in.tumcampusapp.component.other.settings.UserPreferencesActivity;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

import static de.tum.in.tumcampusapp.utils.Const.CARD_POSITION_PREFERENCE_SUFFIX;

/**
 * Base class for all cards
 */
public abstract class Card implements Comparable<Card> {
    public static final String DISCARD_SETTINGS_START = "discard_settings_start";
    static final String DISCARD_SETTINGS_PHONE = "discard_settings_phone";

    // Context related stuff
    protected Context mContext;

    // UI Elements
    protected View mCard;
    protected LinearLayout mLinearLayout;
    protected TextView mTitleView;
    private final String mSettings;

    // Settings for showing this card on start page or as notification
    // Default values set for restore card, no internet card, etc.
    private boolean mShowStart = true;
    boolean mShowPhone;

    private final int cardType;

    /**
     * Card constructor for special cards that don't have a preference screen
     *
     * @param context Context
     */
    public Card(int cardType, Context context) {
        this(cardType, context, null);
    }

    /**
     * Standard card constructor.
     *
     * @param context  Context
     * @param settings Preference key prefix used for all preferences belonging to that card
     */
    public Card(int cardType, Context context, String settings) {
        this.cardType = cardType;
        mSettings = settings;
        mContext = context;
    }

    /**
     * Card constructor that can set special default values for wear and phone
     *
     * @param context      Context
     * @param settings     Preference key prefix used for all preferences belonging to that card
     * @param phoneDefault True if notifications should by default be displayed on the phone
     */
    public Card(int cardType, Context context, String settings, boolean phoneDefault) {
        this(cardType, context, settings);

        mShowStart = Utils.getSettingBool(mContext, settings + "_start", true);
        mShowPhone = Utils.getSettingBool(mContext, settings + "_phone", phoneDefault);
    }

    /**
     * Gets the card type
     *
     * @return Returns an individual integer for each card type
     */
    public final int getType() {
        return cardType;
    }

    /**
     * Updates the Cards content.
     * Override this method, if the card contains any dynamic content, that is not already in its XML
     *
     * @param viewHolder The Card specific view holder
     */
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        mContext = viewHolder.itemView.getContext();
        ImplicitCounter.countCard(mContext, this);
    }

    /**
     * Adds a new text view to the main card layout
     *
     * @param text Text that should be shown
     * @return Handle to the {@link TextView}
     */
    protected TextView addTextView(CharSequence text) {
        TextView textview = new TextView(mContext);
        textview.setText(text);

        //Give some space to the other stuff on the card
        int padding = (int) mContext.getResources()
                                    .getDimension(R.dimen.card_text_padding);
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
     * Must be called after information has been set
     * Adds the card to CardManager if not dismissed before and notifies the user
     */
    public void apply() {
        // Should be shown on start page?
        if (mShowStart) {
            SharedPreferences prefs = mContext.getSharedPreferences(DISCARD_SETTINGS_START, 0);
            if (shouldShow(prefs)) {
                CardManager.addCard(this);
            }
        }
    }

    /**
     * Determines if the card should be shown. Decision is based on the given SharedPreferences.
     * This method should be overridden in most cases.
     *
     * @return returns true if the card should be shown
     */
    protected boolean shouldShow(SharedPreferences prefs) {
        return true;
    }



    /**
     * Tells the list adapter and indirectly the
     * SwipeDismissList if the item is dismissible
     * The restore card is not dismissible.
     */
    public boolean isDismissible() {
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
        e.putBoolean(mSettings + "_phone", false);
        e.apply();
    }

    public int getPosition() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getInt(String.format("%s%s", this.getClass()
                                                      .getSimpleName(), CARD_POSITION_PREFERENCE_SUFFIX), -1);
    }

    public void setPosition(int position) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Editor e = prefs.edit();
        e.putInt(String.format("%s%s", this.getClass()
                                           .getSimpleName(), CARD_POSITION_PREFERENCE_SUFFIX), position);
        e.apply();
    }

    @Override
    public int compareTo(@NonNull Card card) {
        return Integer.compare(getPosition(), card.getPosition());
    }

    @Nullable
    public RemoteViews getRemoteViews(Context context) {
        return null;
    }

    /**
     * @return Should return the intent that should be launched if the card
     * or the notification gets clicked, null if nothing should happen
     */
    public abstract Intent getIntent();

    /**
     * @return a unique identifier among the type of the card
     */
    public abstract int getId();

    /**
     * Save information about the dismissed card/notification to decide later if the card should be shown again
     *
     * @param editor Editor to be used for saving values
     */
    protected abstract void discard(Editor editor);
}
