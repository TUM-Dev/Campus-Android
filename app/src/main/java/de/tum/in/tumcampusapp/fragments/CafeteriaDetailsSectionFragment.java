package de.tum.in.tumcampusapp.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.CafeteriaPrices;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampusapp.models.managers.OpenHoursManager;
import de.tum.in.tumcampusapp.services.FavoriteDishReceiver;

/**
 * Fragment for each cafeteria-page.
 */
public class CafeteriaDetailsSectionFragment extends Fragment {

    /**
     * Inflates the cafeteria menu layout.
     * This is put into an extra static method to be able to
     * reuse it in {@link de.tum.in.tumcampusapp.cards.CafeteriaMenuCard}
     *
     * @param rootView    Parent layout
     * @param cafeteriaId Cafeteria id
     * @param dateStr     Date in yyyy-mm-dd format
     * @param big         True to show big lines
     */
    public static List<View> showMenu(LinearLayout rootView, int cafeteriaId, String dateStr, boolean big) {
        // initialize a few often used things
        final Context context = rootView.getContext();
        final Map<String, String> rolePrices = CafeteriaPrices.getRolePrices(context);
        final int padding = (int) context.getResources().getDimension(R.dimen.card_text_padding);
        List<View> addedViews = new ArrayList<>();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Get menu items
        Cursor cursorCafeteriaMenu = new CafeteriaMenuManager(context).getTypeNameFromDbCard(cafeteriaId, dateStr);

        TextView textview;
        if (!big) {
            // Show opening hours
            OpenHoursManager lm = new OpenHoursManager(context);
            textview = new TextView(context);
            textview.setText(lm.getHoursByIdAsString(context, cafeteriaId, Utils.getDate(dateStr)));
            textview.setTextColor(ContextCompat.getColor(context, R.color.sections_green));
            rootView.addView(textview);
            addedViews.add(textview);
        }

        // Show cafeteria menu
        String curShort = "";
        if (cursorCafeteriaMenu.moveToFirst()) {
            do {
                String typeShort = cursorCafeteriaMenu.getString(3);
                String typeLong = cursorCafeteriaMenu.getString(0);
                String menu = cursorCafeteriaMenu.getString(1);

                // Skip unchecked categories if showing card
                boolean shouldShow = Utils.getSettingBool(context, "card_cafeteria_" + typeShort,
                        typeShort.equals("tg") || typeShort.equals("ae"));
                if (!big && !shouldShow) {
                    continue;
                }

                // Add header if we start with a new category
                if (!typeShort.equals(curShort)) {
                    curShort = typeShort;
                    View view = inflater.inflate(big ? R.layout.list_header_big : R.layout.card_list_header, rootView, false);
                    textview = (TextView) view.findViewById(R.id.list_header);
                    textview.setText(typeLong.replaceAll("[0-9]", "").trim());
                    rootView.addView(view);
                    addedViews.add(view);
                }

                // Show menu item
                SpannableString text = menuToSpan(context, big ? menu : prepare(menu));
                int dishId=cursorCafeteriaMenu.getInt(2);
                if (rolePrices.containsKey(typeLong)) {
                    // If price is available
                    View view = inflater.inflate(big ? R.layout.price_line_big : R.layout.card_price_line , rootView, false);
                    textview = (TextView) view.findViewById(R.id.line_name);
                    TextView priceView = (TextView) view.findViewById(R.id.line_price);
                     //toggle button (star) mark dish as favorite
                    final ToggleButton favDish=(ToggleButton)view.findViewById(R.id.favortieDish);
                    favDish.setOnClickListener(new View.OnClickListener() {
                        /**
                         * when dish marked as favorite create an alarm on specific date using calendar and alarmManager
                         * call the FavoriteDishReceiver
                         */
                        @Override
                        public void onClick(View v) {
                            /**
                             * if checked mark dish as favorite and create Notification on next valid date for dish
                             * update local database set dish as favorite
                             */

                            if(favDish.isChecked())
                            {
                                //new CafeteriaMenuManager(context).insertFavoriteDish(Integer.parseInt(favDish.getTag().toString()));

                               /*Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.MONTH, 6);
                                calendar.set(Calendar.YEAR, 2016);
                                calendar.set(Calendar.DAY_OF_MONTH, 19);
                                calendar.set(Calendar.HOUR_OF_DAY, 5);
                                calendar.set(Calendar.MINUTE, 00);
                                calendar.set(Calendar.SECOND, 0);
                                calendar.set(Calendar.AM_PM,Calendar.AM);
                                */

                                Intent myIntent = new Intent(context, FavoriteDishReceiver.class);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);
                                AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis()+5000, pendingIntent);
                                Toast.makeText(context,favDish.getTag().toString(),Toast.LENGTH_SHORT).show();
                            }
                            /**
                             *update local database set dish as not favorite
                             * remove alarms
                             */
                            else {
                                //new CafeteriaMenuManager(context).DeleteFavoriteDish(Integer.parseInt(favDish.getTag().toString()));
                            }
                        }
                    });

                    /**
                     * saved dish id in the favoriteDishButton tag.
                     * onButton checked getTag->DishID and mark it as favorite
                     */
                    favDish.setTag(dishId +"");
                    textview.setText(text);
                    priceView.setText(String.format("%s €", rolePrices.get(typeLong)));
                    rootView.addView(view);
                    addedViews.add(view);
                } else {
                    // Without price
                    textview = new TextView(context);
                    textview.setText(text);
                    textview.setPadding(padding, padding, padding, padding);
                    rootView.addView(textview);
                    addedViews.add(textview);
                }
            } while (cursorCafeteriaMenu.moveToNext());
        }
        cursorCafeteriaMenu.close();
        return addedViews;
    }

    /**
     * Converts menu text to {@link SpannableString}.
     * Replaces all (v), ... annotations with images
     *
     * @param context Context
     * @param menuString    Text with annotations
     * @return Spannable text with images
     */
    public static SpannableString menuToSpan(Context context, String menuString) {
        int len;
        String menu = menuString;
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

    /**
     * Replaces all annotations that cannot be replaces with images such as (1), ...
     *
     * @param menuString Text to delete annotations from
     * @return Text without un-replaceable annotations
     */
    private static String prepare(String menuString) {
        int len;
        String menu = menuString;
        do {
            len = menu.length();
            menu = menu.replaceFirst("\\(([A-Za-z0-9]+),", "($1)(");
        } while (menu.length() > len);
        return menu.replaceAll("\\(([1-9]|10|11)\\)", "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_cafeteriadetails_section, container, false);
        LinearLayout root = (LinearLayout) rootView.findViewById(R.id.layout);
        int cafeteriaId = getArguments().getInt(Const.CAFETERIA_ID);
        String date = getArguments().getString(Const.DATE);
        showMenu(root, cafeteriaId, date, true);
        return rootView;
    }
}
