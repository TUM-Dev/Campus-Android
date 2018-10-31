package de.tum.`in`.tumcampusapp.component.other.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarActivity
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradesActivity
import de.tum.`in`.tumcampusapp.component.tumui.lectures.activity.LecturesPersonalActivity
import de.tum.`in`.tumcampusapp.component.tumui.person.PersonSearchActivity
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeesActivity
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.BarrierFreeInfoActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity
import de.tum.`in`.tumcampusapp.component.ui.chat.activity.ChatRoomsActivity
import de.tum.`in`.tumcampusapp.component.ui.news.NewsActivity
import de.tum.`in`.tumcampusapp.component.ui.openinghour.OpeningHoursListActivity
import de.tum.`in`.tumcampusapp.component.ui.overview.MainActivity
import de.tum.`in`.tumcampusapp.component.ui.studyroom.StudyRoomsActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.EventsActivity
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportationActivity
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoActivity
import de.tum.`in`.tumcampusapp.utils.Const

object NavigationManager {

    private val topLevelActivities = setOf(
            MainActivity::class.java,
            CalendarActivity::class.java,
            LecturesPersonalActivity::class.java,
            ChatRoomsActivity::class.java,
            GradesActivity::class.java,
            TuitionFeesActivity::class.java,
            CafeteriaActivity::class.java,
            StudyRoomsActivity::class.java,
            RoomFinderActivity::class.java,
            TransportationActivity::class.java,
            PersonSearchActivity::class.java,
            NewsActivity::class.java,
            EventsActivity::class.java,
            BarrierFreeInfoActivity::class.java,
            OpeningHoursListActivity::class.java,
            KinoActivity::class.java
    )

    // TODO: Documentation

    fun open(current: Activity, menuItem: MenuItem) {
        current.startActivity(menuItem.intent)

        if (menuItem.intent.getBooleanExtra(Const.SHOW_DRAWER, false)) {
            current.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }
    }

    fun open(context: Context, destination: NavigationDestination) {
        when (destination) {
            is SystemActivity -> openActivity(context, destination)
            is SystemIntent -> openIntent(context, destination)
        }
    }

    private fun openActivity(context: Context, activity: SystemActivity) {
        val intent = Intent(context, activity.clazz)
        activity.options?.let { intent.putExtras(it) }

        val isTopLevelActivity = activity.clazz in topLevelActivities
        if (isTopLevelActivity) {
            intent.putExtra(Const.SHOW_DRAWER, true)
        }

        context.startActivity(intent).also {
            val currentActivity = context as? Activity
            if (isTopLevelActivity) {
                currentActivity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            }
        }
    }

    private fun openIntent(context: Context, intent: SystemIntent) {
        context.startActivity(intent.intent)
    }

    fun closeActivity(current: AppCompatActivity) {
        if (current.supportFragmentManager.backStackEntryCount > 0) {
            current.supportFragmentManager.popBackStackImmediate()
            return
        }

        val isNavigationItem = current::class.java in topLevelActivities
        val showDrawer = current.intent.extras?.getBoolean(Const.SHOW_DRAWER) ?: false

        if (isNavigationItem && showDrawer) {
            openNavigationDrawer(current)
            return
        } else if (isNavigationItem) {
            current.onBackPressed()
            return
        }

        val upIntent = NavUtils.getParentActivityIntent(current) ?: return
        upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        if (NavUtils.shouldUpRecreateTask(current, upIntent)) {
            TaskStackBuilder.create(current)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities()
        } else {
            NavUtils.navigateUpTo(current, upIntent)
        }
    }

    private fun openNavigationDrawer(activity: AppCompatActivity) {
        val baseActivity = activity as? BaseActivity
        baseActivity?.openDrawer()
    }

    fun onBackPressed(current: Activity): Boolean {
        val isTopLevel = current::class.java in topLevelActivities
        val showDrawer = current.intent.extras?.getBoolean(Const.SHOW_DRAWER) ?: false
        val parentName = current.parentActivityIntent?.component?.className

        if (isTopLevel && showDrawer && parentName == MainActivity::class.java.name) {
            val intent = Intent(current, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            current.startActivity(intent)
            current.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            return true
        }

        return false
    }

}
