package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.RequestCreator
import de.tum.`in`.tumcampusapp.component.other.generic.drawer.SideNavigationItem

/**
 * Executes the block and return null in case of an [Exception].
 *
 * @param block The block of code to execute
 */
inline fun <T> tryOrNull(block: () -> T): T? {
    return try {
        block()
    } catch (_: Exception) {
        null
    }
}

fun RequestCreator.into(target: ImageView, completion: () -> Unit) {
    into(target, object : Callback {
        override fun onSuccess() {
            completion()
        }

        override fun onError(e: java.lang.Exception?) {
            completion()
        }
    })
}

val Menu.items: List<MenuItem>
    get() = (0 until size()).map { getItem(it) }

val Menu.allItems: List<MenuItem>
    get() {
        return items.flatMap { item ->
            if (item.hasSubMenu()) {
                item.subMenu.allItems
            } else {
                listOf(item)
            }
        }
    }

fun Menu.add(context: Context, item: SideNavigationItem, options: Bundle = Bundle()) {
    add(item.titleRes)
            .apply {
                setIcon(item.iconRes)
                intent = Intent(context, item.activity).apply { putExtras(options) }
            }
}
