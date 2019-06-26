package de.tum.`in`.tumcampusapp.component.other.navigation

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseNavigationActivity
import de.tum.`in`.tumcampusapp.component.other.generic.drawer.NavItem
import de.tum.`in`.tumcampusapp.component.ui.overview.MainFragment
import org.jetbrains.anko.browse

class NavManager(
    private val context: Context
) {

    fun open(navItem: NavItem) {
        when (navItem) {
            is NavItem.FragmentDestination -> open(context, navItem)
            is NavItem.ActivityDestination -> open(context, navItem)
        }
    }

    private fun open(context: Context, navItem: NavItem.FragmentDestination) {
        val fragment = Fragment.instantiate(context, navItem.fragment.name)
        openFragment(fragment)
    }

    private fun open(context: Context, navItem: NavItem.ActivityDestination) {
        val intent = Intent(context, navItem.activity)
        context.startActivity(intent)
    }

    fun open(destination: NavDestination) {
        when (destination) {
            is NavDestination.Fragment -> {
                val fragment = Fragment.instantiate(context, destination.clazz.name, destination.args)
                openFragment(fragment)
            }
            is NavDestination.Activity -> {
                val intent = Intent(context, destination.clazz)
                intent.putExtras(destination.args)
                context.startActivity(intent)
            }
            is NavDestination.Link -> context.browse(destination.url)
        }
    }

    fun openFragment(fragment: Fragment) {
        val activity = context as? BaseNavigationActivity ?: return
        if (fragment is MainFragment) {
            activity.supportFragmentManager.addToClearedBackStack(R.id.contentFrame, fragment)
            return
        }

        activity
            .supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fadein, R.anim.fadeout)
            .replace(R.id.contentFrame, fragment)
            .ensureBackToHome(activity)
            .commit()
    }

    private fun FragmentTransaction.ensureBackToHome(
        activity: BaseNavigationActivity
    ): FragmentTransaction {
        if (activity.supportFragmentManager.backStackEntryCount == 0) {
            addToBackStack(null)
        }
        return this
    }

    private fun FragmentManager.addToClearedBackStack(
        containerViewId: Int,
        fragment: Fragment
    ) {
        while (backStackEntryCount > 0) {
            popBackStackImmediate()
        }

        beginTransaction()
            .replace(containerViewId, fragment)
            .commit()
    }

}
