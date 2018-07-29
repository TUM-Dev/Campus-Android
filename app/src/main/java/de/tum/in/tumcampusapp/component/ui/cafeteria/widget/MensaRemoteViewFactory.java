package de.tum.in.tumcampusapp.component.ui.cafeteria.widget;

import android.content.Context;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaPrices;

public class MensaRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final Pattern COMPILE = Pattern.compile("\\([^\\)]+\\)");

    private final Context mApplicationContext;
    private List<CafeteriaMenu> mMenus = new ArrayList<>();

    public MensaRemoteViewFactory(Context context) {
        this.mApplicationContext = context;
    }

    @Override
    public void onCreate() {
        CafeteriaManager mensaManager = new CafeteriaManager(mApplicationContext);
        mMenus = mensaManager.getBestMatchCafeteriaMenus();
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
        return mMenus.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        CafeteriaMenu currentItem = mMenus.get(position);

        RemoteViews rv = new RemoteViews(mApplicationContext.getPackageName(), R.layout.mensa_widget_item);

        String menuContent = COMPILE.matcher(currentItem.getName())
                                    .replaceAll("")
                                    .trim();
        String menuText = mApplicationContext.getString(
                R.string.menu_with_long_type_format_string, menuContent, currentItem.getTypeLong());
        rv.setTextViewText(R.id.menu_content, menuText);

        String price = CafeteriaPrices.INSTANCE.getPrice(mApplicationContext, currentItem.getTypeLong());
        if (price != null) {
            rv.setTextViewText(R.id.menu_price, price + " €");
        }

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
