package de.tum.in.tumcampus.services;

import android.content.Context;
import android.content.Intent;
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
public class MVVWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MensaRemoteViewFactory(this.getApplicationContext(), intent);
    }

}

class MVVRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory{

    private Context applicationContext;
    private CafeteriaManager mensaManager;
    private List<CafeteriaMenu> mensaMenu;
    private String mensaName;

    public MVVRemoteViewFactory(Context applicationContext, Intent intent) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onCreate() {
        mensaManager = new CafeteriaManager(applicationContext);

        // Map of the name of the best mensa and list of its Menu
        Map<String, List<CafeteriaMenu>> currentMensa = mensaManager.getBestMatchMensaInfo(applicationContext);
        if (currentMensa != null) {
            mensaName = currentMensa.keySet().iterator().next();
            mensaMenu = currentMensa.get(mensaName);
        }else
            Utils.log("Error! Could not get list of menus for the mensa widget ");
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

}
