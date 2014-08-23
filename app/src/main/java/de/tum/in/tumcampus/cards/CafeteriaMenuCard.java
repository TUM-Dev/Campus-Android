package de.tum.in.tumcampus.cards;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.CafeteriaDetailsActivity;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.fragments.CafeteriaDetailsSectionFragment;
import de.tum.in.tumcampus.models.CafeteriaMenu;

import static de.tum.in.tumcampus.activities.CafeteriaDetailsActivity.menuToSpan;
import static de.tum.in.tumcampus.models.managers.CardManager.CARD_CAFETERIA;


public class CafeteriaMenuCard extends Card {
    private static final String CAFETERIA_DATE = "cafeteria_date";
    private String mCafeteriaId;
    private String mCafeteriaName;
    private List<CafeteriaMenu> mMenus;
    private Date mDate;

    public CafeteriaMenuCard(Context context) {
        super(context, "card_cafeteria_setting");
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
    public View getCardView(Context context, ViewGroup parent) {
        super.getCardView(context, parent);
        mDateView.setVisibility(View.VISIBLE);
        mDateView.setText(SimpleDateFormat.getDateInstance().format(mDate));

        HashMap<String, String> rolePrices = CafeteriaDetailsSectionFragment.getRolePrices(mContext);

        addHeader("Tagesgerichte");
        String curShort = "tg";
        for (CafeteriaMenu menu : mMenus) {
            if (menu.typeShort.equals("bei"))
                continue;
            if (!menu.typeShort.equals(curShort)) {
                curShort = menu.typeShort;
                addHeader(menu.typeLong);
            }
            if (rolePrices.containsKey(menu.typeLong))
                addPriceline(menuToSpan(mContext, menu.name), rolePrices.get(menu.typeLong) + " €");
            else
                addTextView(menuToSpan(mContext, menu.name));
        }
        return mCard;
    }

    private void addTextView(SpannableString text) {
        TextView textview = new TextView(mContext);
        textview.setText(text);
        textview.setPadding(10, 10, 10, 10);
        mLinearLayout.addView(textview);
    }

    private void addPriceline(SpannableString title, String price) {
        View view = mInflater.inflate(R.layout.card_price_line, mLinearLayout, false);
        TextView textview = (TextView) view.findViewById(R.id.line_name);
        TextView priceview = (TextView) view.findViewById(R.id.line_price);
        textview.setText(title);
        priceview.setText(price);
        mLinearLayout.addView(view);
    }

    public void setCardMenus(String id, String name, Date date, List<CafeteriaMenu> menus) {
        mCafeteriaId = id;
        mCafeteriaName = name;
        mDate = date;
        mMenus = menus;
    }

    @Override
    public Intent getIntent() {
        Intent i = new Intent(mContext, CafeteriaDetailsActivity.class);
        i.putExtra(Const.CAFETERIA_ID, mCafeteriaId);
        i.putExtra(Const.CAFETERIA_NAME, mCafeteriaName);
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
        //mDateView.setText(SimpleDateFormat.getDateInstance().format(mDate));

        HashMap<String, String> rolePrices = CafeteriaDetailsSectionFragment.getRolePrices(mContext);

        NotificationCompat.WearableExtender morePageNotification =
                new NotificationCompat.WearableExtender();

        String allContent = "", firstContent = "";
        for (CafeteriaMenu menu : mMenus) {
            if (menu.typeShort.equals("bei"))
                continue;

            NotificationCompat.Builder pageNotification =
                    new NotificationCompat.Builder(mContext)
                            .setContentTitle(menu.typeLong);

            String content = menu.name;
            if (rolePrices.containsKey(menu.typeLong))
                content +=  "  "+rolePrices.get(menu.typeLong) + " €";

            content = content.replaceAll("\\([A-Za-z 0-9,]+\\)", "").trim();
            pageNotification.setContentText(content);
            if(menu.typeShort.equals("tg")) {
                allContent += content + "\n";
            }
            if(firstContent.isEmpty()) {
                firstContent = content+"...";
            }

            morePageNotification.addPage(pageNotification.build());
        }

        notificationBuilder.setWhen(mDate.getTime());
        notificationBuilder.setContentText(firstContent);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(allContent));
        return morePageNotification.extend(notificationBuilder).build();
    }
}
