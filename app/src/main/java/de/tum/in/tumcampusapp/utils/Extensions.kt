package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Callback
import com.squareup.picasso.RequestCreator
import de.tum.`in`.tumcampusapp.component.other.generic.drawer.NavItem
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

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
    into(
        target,
        object : Callback {
            override fun onSuccess() {
                completion()
            }

            override fun onError(e: java.lang.Exception?) {
                completion()
            }
        }
    )
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

operator fun Menu.plusAssign(item: NavItem) {
    add(item.titleRes).apply { setIcon(item.iconRes) }
}

fun TextView.setTextOrHide(resId: Int?) {
    resId?.let {
        text = context.getString(it)
        return
    }

    visibility = View.GONE
}

fun MaterialButton.setTextOrHide(resId: Int?) {
    resId?.let {
        text = context.getString(it)
        return
    }

    visibility = View.GONE
}

fun ImageView.setImageResourceOrHide(resId: Int?) {
    resId?.let {
        setImageResource(it)
        return
    }

    visibility = View.GONE
}

fun DrawerLayout.closeDrawers(callback: () -> Unit) {
    addDrawerListener(object : DrawerLayout.DrawerListener {
        override fun onDrawerStateChanged(newState: Int) = Unit

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit

        override fun onDrawerClosed(drawerView: View) {
            callback()
            removeDrawerListener(this)
        }

        override fun onDrawerOpened(drawerView: View) = Unit
    })
    closeDrawers()
}

fun <T> LiveData<T>.observe(owner: LifecycleOwner, callback: (T?) -> Unit) {
    observe(owner, Observer<T> { value -> callback(value) })
}

fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, callback: (T) -> Unit) {
    observe(
        owner,
        Observer<T> { value ->
            value?.let {
                callback(it)
            }
        }
    )
}

fun TextView.addCompoundDrawablesWithIntrinsicBounds(
    start: Drawable? = null,
    top: Drawable? = null,
    right: Drawable? = null,
    bottom: Drawable? = null
) {
    setCompoundDrawablesWithIntrinsicBounds(start, top, right, bottom)
}

fun TextView.addCompoundDrawablesWithIntrinsicBounds(
    start: Int = 0,
    top: Int = 0,
    right: Int = 0,
    bottom: Int = 0
) {
    setCompoundDrawablesWithIntrinsicBounds(start, top, right, bottom)
}

operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    this.add(disposable)
}

fun <T1, T2> List<T1>.splitOnChanged(transform: (T1) -> T2): List<List<T1>> {
    val results = mutableListOf<MutableList<T1>>()
    var latestValue: T2? = null

    for (item in this) {
        val currentSubList = results.lastOrNull() ?: mutableListOf()
        val value = transform(item)
        if (value != latestValue) {
            // Add item to a new sub-list
            results.add(mutableListOf(item))
            latestValue = value
        } else {
            // Add item to existing sub-list
            currentSubList.add(item)
        }
    }
    return results.toList()
}

fun View.margin(left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.run { leftMargin = dpToPx(this) }
        top?.run { topMargin = dpToPx(this) }
        right?.run { rightMargin = dpToPx(this) }
        bottom?.run { bottomMargin = dpToPx(this) }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
