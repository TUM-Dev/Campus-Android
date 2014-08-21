package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.CafeteriaDetailsActivity;
import de.tum.in.tumcampus.auxiliary.CafetariaPrices;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.CafeteriaMenu;
import de.tum.in.tumcampus.models.managers.CardManager;

import static de.tum.in.tumcampus.models.managers.CardManager.CARD_CAFETERIA;


public class CafeteriaMenuCard extends Card {
    private static final String CAFETERIA_DATE = "cafeteria_date";
    private String mCafeteriaId;
    private String mCafeteriaName;
    private List<CafeteriaMenu> mMenus;
    private Date mDate;

    @Override
    public int getTyp() {
        return CARD_CAFETERIA;
    }

    public static SpannableString menuToSpan(Context context, String menu) {
        int len;
        do {
            len = menu.length();
            menu = menu.replaceFirst("\\(([A-Za-z0-9]+),", "($1)(");
        } while(menu.length()>len);
        SpannableString text = new SpannableString(menu);
        replaceWithImg(context, menu, text, "(v)",R.drawable.meal_vegan);
        replaceWithImg(context, menu, text, "(f)",R.drawable.meal_veggie);
        replaceWithImg(context, menu, text, "(R)",R.drawable.meal_beef);
        replaceWithImg(context, menu, text, "(S)",R.drawable.meal_pork);
        replaceWithImg(context, menu, text, "(GQB)",R.drawable.ic_gqb);
        replaceWithImg(context, menu, text, "(99)",R.drawable.meal_alcohol);
        return text;
    }

    private static void replaceWithImg(Context context, String menu, SpannableString text, String sym, int drawable) {
        int ind = menu.indexOf(sym);
        while(ind>=0) {
            ImageSpan is = new ImageSpan(context, drawable);
            text.setSpan(is, ind, ind + sym.length(), 0);
            ind = menu.indexOf(sym,ind+sym.length());
        }
    }

    @Override
    public View getView(Context context, ViewGroup parent) {
        super.getView(context, parent);
        mTitleView.setText(mCafeteriaName);
        mDateView.setVisibility(View.VISIBLE);
        mDateView.setText(SimpleDateFormat.getDateInstance().format(mDate));

        HashMap<String, String> rolePrices;
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String type = sharedPrefs.getString(Const.ROLE, "0");
        if (type.equals("0")) {
            rolePrices = CafetariaPrices.student_prices;
        } else if (type.equals("1")) {
            rolePrices = CafetariaPrices.employee_prices;
        } else if (type.equals("2")) {
            rolePrices = CafetariaPrices.guest_prices;
        } else {
            rolePrices = CafetariaPrices.student_prices;
        }

        addHeader(context,"Tagesgerichte");
        String curShort = "tg";
        for(CafeteriaMenu menu : mMenus) {
            if(menu.typeShort.equals("bei"))
                continue;
            if(!menu.typeShort.equals(curShort)) {
                curShort = menu.typeShort;
                addHeader(context, menu.typeLong);
            }
            if (rolePrices.containsKey(menu.typeLong))
                addPriceline(menuToSpan(context, menu.name), rolePrices.get(menu.typeLong) + " €");
            else
                addTextView(context, menuToSpan(context, menu.name));
        }
        return mCard;
    }

    private void addTextView(Context context, SpannableString text) {
        TextView textview = new TextView(context);
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
    public void onCardClick(Context context) {
        Intent i = new Intent(context, CafeteriaDetailsActivity.class);
        i.putExtra(Const.CAFETERIA_ID, mCafeteriaId);
        i.putExtra(Const.CAFETERIA_NAME, mCafeteriaName);
        context.startActivity(i);
    }

    @Override
    public void discard() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CardManager.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(CAFETERIA_DATE, mDate.getTime());
        editor.commit();
    }

    @Override
    public boolean apply() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CardManager.getContext());
        long prevDate = prefs.getLong(CAFETERIA_DATE,0);
        if(prevDate<mDate.getTime()) {
            CardManager.addCard(this);
            return true;
        }
        return false;
    }
}
