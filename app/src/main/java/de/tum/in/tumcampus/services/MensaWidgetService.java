package de.tum.in.tumcampus.services;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;
import java.util.Map;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.CafeteriaPrices;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.CafeteriaMenu;
import de.tum.in.tumcampus.models.managers.CafeteriaManager;

/**
 * Created by a2k on 7/6/2015.
 */
public class MensaWidgetService extends RemoteViewsService {
    public static String ACTION_TELL_MENSA_NAME = "de.tum.in.tumcampus.services.MensaWidgetService.action.TELL_MENSA_NAME";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MensaRemoteViewFactory(this.getApplicationContext(), intent);
    }

}

class MensaRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory{
    private Context applicationContext;
    private int mensaAppWidgetId;
    private CafeteriaManager mensaManager;
    private List<CafeteriaMenu> mensaMenu;
    private String mensaName;

    public MensaRemoteViewFactory(Context applicationContext, Intent intent) {
        this.applicationContext = applicationContext;
        mensaAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }


    @Override
    public void onCreate() {
        mensaManager = new CafeteriaManager(applicationContext);
        Map<String, List<CafeteriaMenu>> currentMensa = mensaManager.getBestMatchMensaInfo(applicationContext);
        if (currentMensa != null) {
            mensaName = currentMensa.keySet().iterator().next();
            broadcastMensaName(mensaName);
            mensaMenu = currentMensa.get(mensaName);
        }else
            Utils.log("Error! Could not get list of menus for the mensa widget ");
    }

    private void broadcastMensaName(String name){
        Intent intent = new Intent();
        intent.setAction(MensaWidgetService.ACTION_TELL_MENSA_NAME);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mensaAppWidgetId);
        intent.putExtra("mensa_name",name);
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent);
        Utils.log("broadcasting mensa's name: " + name);
}

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        if (mensaMenu != null)
            return mensaMenu.size();
        else
            return 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(applicationContext.getPackageName(), R.layout.mensa_widget_item);
        CafeteriaMenu current_item = mensaMenu.get(position);
        if (current_item != null) {
            rv.setTextViewText(R.id.menu_type, current_item.typeShort);

            String menuContent = current_item.name.replaceAll("\\([^\\)]+\\)", "").trim();
            rv.setTextViewText(R.id.menu_content, menuContent);

            String price = CafeteriaPrices.getPrice(applicationContext, current_item.typeLong);
            if ( price != null)
                price += " €";
            else
                price = "____€";

            rv.setTextViewText(R.id.menu_price, price);
            return rv;
        }
        return null;
    }



    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    /**
     * Converts menu text to {@link android.text.SpannableString}.
     * Replaces all (v), ... annotations with images
     *
     * @param context Context
     * @param menu    Text with annotations
     * @return Spannable text with images
     */
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

}
