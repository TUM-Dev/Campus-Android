package de.tum.in.tumcampusapp.widgets.remoteviewfactories;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.CafeteriaPrices;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.CafeteriaManager;
import de.tum.in.tumcampusapp.models.cafeteria.CafeteriaMenu;

public class MensaRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final Pattern COMPILE = Pattern.compile("\\([^\\)]+\\)");
    private final Context applicationContext;
    private List<CafeteriaMenu> mensaMenu;

    public MensaRemoteViewFactory(Context applicationContext, Intent intent) {
        this.applicationContext = applicationContext.getApplicationContext();
    }

    @Override
    public void onCreate() {
        CafeteriaManager mensaManager = new CafeteriaManager(applicationContext);

        // Map of the name of the best mensa and list of its Menu
        Map<String, List<CafeteriaMenu>> currentMensa = mensaManager.getBestMatchMensaInfo(applicationContext);
        if (currentMensa == null) {
            Utils.log("Error! Could not get list of menus for the mensa widget ");
        } else {
            String mensaName = currentMensa.keySet()
                                           .iterator()
                                           .next();
            mensaMenu = currentMensa.get(mensaName);
        }
    }

    @Override
    public void onDataSetChanged() {
        // Noop
    }

    @Override
    public void onDestroy() {
        // Noop
    }

    @Override
    public int getCount() {
        if (mensaMenu == null) {
            return 0;
        }
        return mensaMenu.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        CafeteriaMenu currentItem = mensaMenu.get(position);
        if (currentItem == null) {
            return null;
        }
        RemoteViews rv = new RemoteViews(applicationContext.getPackageName(), R.layout.mensa_widget_item);

        String menuContent = COMPILE.matcher(currentItem.name)
                                    .replaceAll("")
                                    .trim();
        rv.setTextViewText(R.id.menu_content, menuContent + " (" + currentItem.typeShort + ")");

        String price = CafeteriaPrices.getPrice(applicationContext, currentItem.typeLong);
        if (price == null) {
            price = "____";
        }

        rv.setTextViewText(R.id.menu_price, price + " €");
        return rv;
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
