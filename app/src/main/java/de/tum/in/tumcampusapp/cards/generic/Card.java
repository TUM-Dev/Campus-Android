package de.tum.in.tumcampusapp.cards.generic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
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
import de.tum.in.tumcampusapp.activities.UserPreferencesActivity;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.ImplicitCounter;
import de.tum.in.tumcampusapp.managers.CardManager;

/**
 * Base class for all cards
 */
public abstract class Card {
    public static final String DISCARD_SETTINGS_START = "discard_settings_start";
    public static final String DISCARD_SETTINGS_PHONE = "discard_settings_phone";

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
    protected boolean mShowWear;
    protected boolean mShowPhone;

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
     * @param wearDefault  True if notifications should by default be displayed on android wear
     * @param phoneDefault True if notifications should by default be displayed on the phone
     */
    public Card(int cardType, Context context, String settings, boolean wearDefault, boolean phoneDefault) {
        this(cardType, context, settings);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mShowStart = prefs.getBoolean(settings + "_start", true);
        mShowWear = prefs.getBoolean(settings + "_wear", wearDefault);
        mShowPhone = prefs.getBoolean(settings + "_phone", phoneDefault);
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
     * Save information about the dismissed card/notification to decide later if the card should be shown again
     *
     * @param editor Editor to be used for saving values
     */
    protected abstract void discard(Editor editor);

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
     * @return Should return the intent that should be launched if the card
     * or the notification gets clicked, null if nothing should happen
     */
    public abstract Intent getIntent();

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
        e.putBoolean(mSettings + "_wear", false);
        e.putBoolean(mSettings + "_phone", false);
        e.apply();
    }

    /**
     * @return a unique identifier among the type of the card
     */
    public abstract int getId();

    @Nullable
    public RemoteViews getRemoteViews(Context context) {
        return null;
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
                ContextCompat.startActivity(mActivity, i, options.toBundle());
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
            menu.setOnMenuItemClickListener(this);

            menu.show();
            return true;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int i = item.getItemId();
            if (i == R.id.open_card_setting) {// Open card's preference screen
                String key = current.getSettings();
                if (key == null) {
                    return true;
                }

                Intent intent = new Intent(itemView.getContext(), UserPreferencesActivity.class);
                intent.putExtra(Const.PREFERENCE_SCREEN, key);
                itemView.getContext()
                        .startActivity(intent);
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
