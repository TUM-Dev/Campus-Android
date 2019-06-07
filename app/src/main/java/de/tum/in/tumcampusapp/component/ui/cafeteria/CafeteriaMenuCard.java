package de.tum.in.tumcampusapp.component.ui.cafeteria;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.navigation.NavDestination;
import de.tum.in.tumcampusapp.component.ui.cafeteria.fragment.CafeteriaFragment;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus;
import de.tum.in.tumcampusapp.component.ui.overview.CardInteractionListener;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.utils.Const;

/**
 * Card that shows the cafeteria menu
 */
public class CafeteriaMenuCard extends Card {

    private static final String CAFETERIA_DATE = "cafeteria_date";

    private CafeteriaWithMenus mCafeteria;

    public CafeteriaMenuCard(Context context) {
        super(CardManager.CARD_CAFETERIA, context, "card_cafeteria");
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent,
                                                   CardInteractionListener interactionListener) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.card_cafeteria_menu, parent, false);
        return new CafeteriaMenuViewHolder(view, interactionListener);
    }

    @Override
    public void updateViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);

        if (viewHolder instanceof CafeteriaMenuViewHolder) {
            CafeteriaMenuViewHolder holder = (CafeteriaMenuViewHolder) viewHolder;
            holder.bind(mCafeteria);
        }
    }

    @Override
    public int getOptionsMenuResId() {
        return R.menu.card_popup_menu;
    }

    /**
     * Sets the information needed to build the card
     *
     * @param cafeteria The CafeteriaWithMenus object to be displayed in the card
     */
    public void setCafeteriaWithMenus(CafeteriaWithMenus cafeteria) {
        this.mCafeteria = cafeteria;
    }

    public String getTitle() {
        return mCafeteria.getName();
    }

    @Nullable
    @Override
    public NavDestination getNavigationDestination() {
        Bundle bundle = new Bundle();
        bundle.putInt(Const.CAFETERIA_ID, mCafeteria.getId());
        return new NavDestination.Fragment(CafeteriaFragment.class, bundle);
    }

    @Override
    public void discard(@NonNull Editor editor) {
        DateTime date = mCafeteria.getNextMenuDate();
        editor.putLong(CAFETERIA_DATE + "_" + mCafeteria.getId(), date.getMillis());
    }

    @Override
    protected boolean shouldShow(@NonNull SharedPreferences prefs) {
        // the card reappears when the day is over and a new menu will be shown
        final long prevDate = prefs.getLong(CAFETERIA_DATE + "_" + mCafeteria.getId(), 0);
        DateTime date = mCafeteria.getNextMenuDate();
        return prevDate < date.getMillis();
    }

    @Override
    public void hideAlways() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String id = Integer.toString(mCafeteria.getId());
        Set<String> ids = prefs.getStringSet(Const.CAFETERIA_CARDS_SETTING, new HashSet<>());
        if (ids.contains(id)) {
            ids.remove(id);
        } else {
            ids.remove(Const.CAFETERIA_BY_LOCATION_SETTINGS_ID);
        }
        prefs.edit().putStringSet(Const.CAFETERIA_CARDS_SETTING, ids).apply();
    }

}
