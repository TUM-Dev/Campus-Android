package de.tum.in.tumcampusapp.component.ui.cafeteria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.utils.DateUtils;

import static de.tum.in.tumcampusapp.component.ui.cafeteria.details.CafeteriaDetailsSectionFragment.showMenu;
import static de.tum.in.tumcampusapp.component.ui.overview.CardManager.CARD_CAFETERIA;

/**
 * Card that shows the cafeteria menu
 */
public class CafeteriaMenuCard extends Card {

    private static final String CAFETERIA_DATE = "cafeteria_date";

    // TODO: Replace with CafeteriaWithMenus
    private CafeteriaWithMenus mCafeteria;

    //private int mCafeteriaId;
    //private String mCafeteriaName;
    //private Date mDate;
    //private String mDateStr;
    //private List<CafeteriaMenu> mMenus;

    public CafeteriaMenuCard(Context context) {
        super(CARD_CAFETERIA, context, "card_cafeteria");
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_item, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        CardViewHolder cardsViewHolder = (CardViewHolder) viewHolder;
        List<View> addedViews = cardsViewHolder.getAddedViews();
        setMCard(viewHolder.itemView);
        setMLinearLayout(getMCard().findViewById(R.id.card_view));
        setMTitleView(getMCard().findViewById(R.id.card_title));
        getMTitleView().setText(mCafeteriaName);

        // Show date
        TextView mDateView = getMCard().findViewById(R.id.card_date);
        mDateView.setVisibility(View.VISIBLE);
        mDateView.setText(DateFormat.getDateInstance()
                                    .format(mDate));

        //Remove additional views
        for (View view : addedViews) {
            getMLinearLayout().removeView(view);
        }

        // Show cafeteria menu
        cardsViewHolder.setAddedViews(showMenu(getMLinearLayout(), mCafeteriaId, mDateStr, false, mMenus));
    }


    public void setCafeteria(CafeteriaWithMenus cafeteria) {
        mCafeteria = cafeteria;
    }

    public void setCardMenus(CafeteriaWithMenus cafeteria) {
        setCardMenus(
                cafeteria.getId(),
                cafeteria.getName(),
                cafeteria.getNextMenuDate(),
                DateUtils.getDate(cafeteria.getNextMenuDate()),
                cafeteria.getMenus()
        );
    }
    /**
     * Sets the information needed to build the card
     *
     * @param id      Cafeteria id
     * @param name    Cafeteria name
     * @param dateStr Date of the menu in yyyy-mm-dd format
     * @param date    Date of the menu
     * @param menus   List of cafeteria menus
     */
    public void setCardMenus(int id, String name, String dateStr, Date date, List<CafeteriaMenu> menus) {
        mCafeteriaId = id;
        mCafeteriaName = name;
        mDateStr = dateStr;
        mDate = date;
        mMenus = menus;
    }

    @Override
    public Intent getIntent() {
        return mCafeteria.getIntent(getContext());
    }

    @Override
    public void discard(Editor editor) {
        editor.putLong(CAFETERIA_DATE, mDate.getTime());
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        final long prevDate = prefs.getLong(CAFETERIA_DATE, 0);
        return prevDate < mDate.getTime();
    }

    @Override
    public RemoteViews getRemoteViews(Context context, int appWidgetId) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.cards_widget_card);
        remoteViews.setTextViewText(R.id.widgetCardTextView, mCafeteriaName);
        remoteViews.setImageViewResource(R.id.widgetCardImageView, R.drawable.ic_cutlery);
        return remoteViews;
    }
}
