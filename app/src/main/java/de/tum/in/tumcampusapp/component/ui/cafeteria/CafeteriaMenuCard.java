package de.tum.in.tumcampusapp.component.ui.cafeteria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus;
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

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.card_cafeteria_menu, parent, false);
        return new CafeteriaMenuViewHolder(view);
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);

        if (viewHolder instanceof CafeteriaMenuViewHolder) {
            CafeteriaMenuViewHolder holder = (CafeteriaMenuViewHolder) viewHolder;
            holder.bind(mCafeteria);
        }
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

    @Override
    public Intent getIntent() {
        Intent i = new Intent(getContext(), CafeteriaActivity.class);
        i.putExtra(Const.CAFETERIA_ID, mCafeteria.getId());
        return i;
    }

    @Override
    public void discard(Editor editor) {
        DateTime date = mCafeteria.getNextMenuDate();
        editor.putLong(CAFETERIA_DATE, date.getMillis());
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        final long prevDate = prefs.getLong(CAFETERIA_DATE, 0);
        DateTime date = mCafeteria.getNextMenuDate();
        return prevDate < date.getMillis();
    }

}
