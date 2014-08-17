package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.CafetariaPrices;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.CafeteriaMenu;

import static de.tum.in.tumcampus.models.managers.CardManager.CARD_CAFETERIA;

/**
* Created by Florian on 17.08.2014.
*/
public class CafeteriaMenuCard extends Card {
    String mCafeteriaName;
    List<CafeteriaMenu> mMenus;

    @Override
    public int getTyp() {
        return CARD_CAFETERIA;
    }

    @Override
    public View getView(Context context, ViewGroup parent) {
        super.getView(context, parent);
        mTitleView.setText(mCafeteriaName);

        HashMap<String, String> rolePrices = null;
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
            if(menu.typeShort.equals(curShort)) {

                if (rolePrices.containsKey(menu.typeLong))
                    addPriceline(menu.name, rolePrices.get(menu.typeLong) + " €");
                else
                    addTextView(context, menu.name);
            } else {
                curShort = menu.typeShort;
                addHeader(context,menu.typeLong);
                addTextView(context, menu.name);
            }
        }
        return mCard;
    }

    private void addPriceline(String title, String price) {
        View view = mInflater.inflate(R.layout.price_line, mLinearLayout, false);
        TextView textview = (TextView) view.findViewById(R.id.line_name);
        TextView priceview = (TextView) view.findViewById(R.id.line_price);
        textview.setText(title);
        priceview.setText(price);
        mLinearLayout.addView(view);
    }

    public void setCardMenus(String name,List<CafeteriaMenu> menus) {
        mCafeteriaName = name;
        mMenus = menus;
    }
}
