package de.tum.in.tumcampus.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.CafetariaPrices;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.managers.OpenHoursManager;

/**
 * Fragment for each cafeteria-page.
 */
public class CafeteriaDetailsSectionFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_cafeteriadetails_section, container, false);
        LinearLayout root = (LinearLayout) rootView.findViewById(R.id.layout);

        int cafeteriaId = getArguments().getInt(Const.CAFETERIA_ID);
        String date = getArguments().getString(Const.DATE);

		showMenu(root, cafeteriaId, date, true);
		return rootView;
	}

    public static void showMenu(LinearLayout rootView, int cafeteriaId, String dateStr, boolean big) {
        // initialize a few often used things
        final Context context = rootView.getContext();
        final HashMap<String, String> rolePrices = CafeteriaDetailsSectionFragment.getRolePrices(context);
        final int padding = (int)context.getResources().getDimension(R.dimen.card_text_padding);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Get menu items
        Cursor cursorCafeteriaMenu = new CafeteriaMenuManager(context).getTypeNameFromDbCard(cafeteriaId, dateStr);

        TextView textview;
        if(!big) {
            // Show opening hours
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            OpenHoursManager lm = new OpenHoursManager(context);
            textview = new TextView(context);
            try {
                textview.setText(lm.getHoursById(context, cafeteriaId, formatter.parse(dateStr)));
            } catch (ParseException e) {
                e.printStackTrace();
                textview.setText(lm.getHoursById(context, cafeteriaId, new Date()));
            }
            textview.setTextColor(context.getResources().getColor(R.color.sections_green));
            rootView.addView(textview);
        }

        // Show cafeteria menu
        String curShort = "";
        if(cursorCafeteriaMenu.moveToFirst()) {
            do {
                String typeShort = cursorCafeteriaMenu.getString(3);
                String typeLong = cursorCafeteriaMenu.getString(0);
                String menu = cursorCafeteriaMenu.getString(1);

                // Skip "Beilagen" if showing card
                if (typeShort.equals("bei") && !big)
                    continue;

                // Add header if we start with a new category
                if (!typeShort.equals(curShort)) {
                    curShort = typeShort;
                    View view = inflater.inflate(big?R.layout.list_header_big:R.layout.card_list_header, rootView, false);
                    textview = (TextView) view.findViewById(R.id.list_header);
                    textview.setText(typeLong.replaceAll("[0-9]", "").trim());
                    rootView.addView(view);
                }

                // Show menu item
                SpannableString text = menuToSpan(context, big ? menu : prepare(menu));
                if (rolePrices.containsKey(typeLong)) {
                    // If price is available
                    View view = inflater.inflate(big?R.layout.price_line_big:R.layout.card_price_line, rootView, false);
                    textview = (TextView) view.findViewById(R.id.line_name);
                    TextView priceview = (TextView) view.findViewById(R.id.line_price);
                    textview.setText(text);
                    priceview.setText(rolePrices.get(typeLong) + " €");
                    rootView.addView(view);
                } else {
                    // Without price
                    textview = new TextView(context);
                    textview.setText(text);
                    textview.setPadding(padding, padding, padding, padding);
                    rootView.addView(textview);
                }
            } while (cursorCafeteriaMenu.moveToNext());
        }
        cursorCafeteriaMenu.close();
	}


    public static HashMap<String, String> getRolePrices(Context context) {
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
        return rolePrices;
    }

    public static SpannableString menuToSpan(Context context, String menu) {
        int len;
        do {
            len = menu.length();
            menu = menu.replaceFirst("\\(([A-Za-z0-9]+),", "($1)(");
        } while (menu.length() > len);
        SpannableString text = new SpannableString(menu);
        replaceWithImg(context, menu, text, "(v)", R.drawable.meal_vegan);
        replaceWithImg(context, menu, text, "(f)", R.drawable.meal_veggie);
        replaceWithImg(context, menu, text, "(R)", R.drawable.meal_beef);
        replaceWithImg(context, menu, text, "(S)", R.drawable.meal_pork);
        replaceWithImg(context, menu, text, "(GQB)", R.drawable.ic_gqb);
        replaceWithImg(context, menu, text, "(99)", R.drawable.meal_alcohol);
        return text;
    }

    private static void replaceWithImg(Context context, String menu, SpannableString text, String sym, int drawable) {
        int ind = menu.indexOf(sym);
        while (ind >= 0) {
            ImageSpan is = new ImageSpan(context, drawable);
            text.setSpan(is, ind, ind + sym.length(), 0);
            ind = menu.indexOf(sym, ind + sym.length());
        }
    }

    private static String prepare(String menu) {
        int len;
        do {
            len = menu.length();
            menu = menu.replaceFirst("\\(([A-Za-z0-9]+),", "($1)(");
        } while (menu.length() > len);
        return menu.replaceAll("\\(([1-9]|10|11)\\)", "");
    }
}
