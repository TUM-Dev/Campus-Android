package de.tum.`in`.tumcampusapp.component.ui.cafeteria.widget

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.DishPrices
import java.util.*

class MensaRemoteViewFactory(private val applicationContext: Context) : RemoteViewsService.RemoteViewsFactory {
    private var menus: List<CafeteriaMenu> = ArrayList()

    override fun onCreate() {
        menus = CafeteriaManager(applicationContext).bestMatchCafeteriaMenus
    }

    override fun onDataSetChanged() { /* Noop */ }

    override fun onDestroy() { /* Noop */ }

    override fun getCount() = menus.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= menus.size) {
            // No idea why this happens, but getViewAt is occasionally called with position == size
            return loadingView
        }

        val (_, _, _, _, dishType, name, _, _) = menus[position]
        val remoteViews = RemoteViews(applicationContext.packageName, R.layout.mensa_widget_item)

        val menuText = applicationContext.getString(
                R.string.menu_with_long_type_format_string, name, dishType)
        remoteViews.setTextViewText(R.id.menu_content, menuText)

        DishPrices.getPrice(applicationContext, dishType)?.let {
            remoteViews.setTextViewText(R.id.menu_price, "$it €")
        }

        return remoteViews
    }

    override fun getLoadingView() = RemoteViews(applicationContext.packageName, R.layout.mensa_widget_loading_item)

    override fun getViewTypeCount() = 1

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds() = true
}
