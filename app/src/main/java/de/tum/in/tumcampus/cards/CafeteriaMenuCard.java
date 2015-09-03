package de.tum.in.tumcampus.cards;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.CafeteriaActivity;
import de.tum.in.tumcampus.auxiliary.CafeteriaPrices;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.CafeteriaMenu;

import static de.tum.in.tumcampus.fragments.CafeteriaDetailsSectionFragment.showMenu;
import static de.tum.in.tumcampus.models.managers.CardManager.CARD_CAFETERIA;

/**
 * Card that shows the cafeteria menu
 */
public class CafeteriaMenuCard extends Card {
    private static final String CAFETERIA_DATE = "cafeteria_date";
    private int mCafeteriaId;
    private String mCafeteriaName;
    private Date mDate;
    private String mDateStr;
    private List<CafeteriaMenu> mMenus;

    public CafeteriaMenuCard(Context context) {
        super(context, "card_cafeteria");
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new Card.CardViewHolder(view);
    }

    @Override
    public int getTyp() {
        return CARD_CAFETERIA;
    }

    @Override
    public String getTitle() {
        return mCafeteriaName;
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        CardViewHolder cardsViewHolder = (CardViewHolder) viewHolder;
        List<View> addedViews = cardsViewHolder.getAddedViews();
        mCard = viewHolder.itemView;
        mLinearLayout = (LinearLayout) mCard.findViewById(R.id.card_view);
        mTitleView = (TextView) mCard.findViewById(R.id.card_title);
        mTitleView.setText(getTitle());

        // Show date
        TextView mDateView = (TextView) mCard.findViewById(R.id.card_date);
        mDateView.setVisibility(View.VISIBLE);
        mDateView.setText(SimpleDateFormat.getDateInstance().format(mDate));

        //Remove additional views
        for (View view : addedViews) {
            mLinearLayout.removeView(view);
        }

        // Show cafeteria menu
        cardsViewHolder.setAddedViews(showMenu(mLinearLayout, mCafeteriaId, mDateStr, false));
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
        Intent i = new Intent(mContext, CafeteriaActivity.class);
        i.putExtra(Const.CAFETERIA_ID, mCafeteriaId);
        return i;
    }

    @Override
    protected void discard(Editor editor) {
        editor.putLong(CAFETERIA_DATE, mDate.getTime());
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        final long prevDate = prefs.getLong(CAFETERIA_DATE, 0);
        return prevDate < mDate.getTime();
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        HashMap<String, String> rolePrices = CafeteriaPrices.getRolePrices(mContext);

        NotificationCompat.WearableExtender morePageNotification =
                new NotificationCompat.WearableExtender();

        String allContent = "", firstContent = "";
        for (CafeteriaMenu menu : mMenus) {
            if (menu.typeShort.equals("bei"))
                continue;

            NotificationCompat.Builder pageNotification =
                    new NotificationCompat.Builder(mContext)
                            .setContentTitle(menu.typeLong.replaceAll("[0-9]", "").trim());

            String content = menu.name;
            if (rolePrices.containsKey(menu.typeLong))
                content += "\n" + rolePrices.get(menu.typeLong) + " €";

            content = content.replaceAll("\\([^\\)]+\\)", "").trim();
            pageNotification.setContentText(content);
            if (menu.typeShort.equals("tg")) {
                if (!allContent.isEmpty())
                    allContent += "\n";
                allContent += content;
            }
            if (firstContent.isEmpty()) {
                firstContent = menu.name.replaceAll("\\([^\\)]+\\)", "").trim() + "...";
            } else {
                morePageNotification.addPage(pageNotification.build());
            }
        }

        notificationBuilder.setWhen(mDate.getTime());
        notificationBuilder.setContentText(firstContent);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(allContent));
        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.wear_cafeteria);
        morePageNotification.setBackground(bm);
        return morePageNotification.extend(notificationBuilder).build();
    }
}
