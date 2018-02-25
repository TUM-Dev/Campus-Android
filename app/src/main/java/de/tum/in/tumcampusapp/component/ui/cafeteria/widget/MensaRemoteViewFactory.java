package de.tum.in.tumcampusapp.component.ui.cafeteria.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaPrices;

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
        Map<String, List<CafeteriaMenu>> menus = mensaManager.getBestMatchMensaInfo(applicationContext)
                                                             .blockingFirst();
        mensaMenu = menus.get(menus.keySet().iterator().next());

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

        String menuContent = COMPILE.matcher(currentItem.getName())
                                    .replaceAll("")
                                    .trim();
        rv.setTextViewText(R.id.menu_content, menuContent + " (" + currentItem.getTypeShort() + ")");

        String price = CafeteriaPrices.INSTANCE.getPrice(applicationContext, currentItem.getTypeLong());
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
