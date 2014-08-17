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

import java.util.HashMap;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.CafeteriaDetailsActivity;
import de.tum.in.tumcampus.auxiliary.CafetariaPrices;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.CafeteriaMenu;

import static de.tum.in.tumcampus.models.managers.CardManager.CARD_CAFETERIA;

/**
* Created by Florian on 17.08.2014.
*/
public class CafeteriaMenuCard extends Card {
    private String mCafeteriaId;
    private String mCafeteriaName;
    private List<CafeteriaMenu> mMenus;

    @Override
    public int getTyp() {
        return CARD_CAFETERIA;
    }

    private SpannableString menuToSpan(String menu, Context context) {
        int len;
        do {
            len = menu.length();
            menu = menu.replaceFirst("\\(([a-z0-9]+),", "($1)(");
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

    private void replaceWithImg(Context context, String menu, SpannableString text, String sym, int drawable) {
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
            if(!menu.typeShort.equals(curShort)) {
                curShort = menu.typeShort;
                addHeader(context, menu.typeLong);
            }
            if (rolePrices.containsKey(menu.typeLong))
                addPriceline(menuToSpan(menu.name,context), rolePrices.get(menu.typeLong) + " €");
            else
                addTextView(context, menuToSpan(menu.name, context));
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
        View view = mInflater.inflate(R.layout.price_line, mLinearLayout, false);
        TextView textview = (TextView) view.findViewById(R.id.line_name);
        TextView priceview = (TextView) view.findViewById(R.id.line_price);
        textview.setText(title);
        priceview.setText(price);
        mLinearLayout.addView(view);
    }

    public void setCardMenus(String id,String name,List<CafeteriaMenu> menus) {
        mCafeteriaId = id;
        mCafeteriaName = name;
        mMenus = menus;
    }

    @Override
    public void onCardClick(Context context) {
        Intent i = new Intent(context, CafeteriaDetailsActivity.class);
        i.putExtra(Const.CAFETERIA_ID, mCafeteriaId);
        i.putExtra(Const.CAFETERIA_NAME, mCafeteriaName);
        context.startActivity(i);
    }
}
