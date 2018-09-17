package de.tum.`in`.tumcampusapp.component.other.generic.drawer

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.NavigationView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.other.settings.UserPreferencesActivity
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarActivity
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackActivity
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
import de.tum.`in`.tumcampusapp.component.ui.overview.InformationActivity
import de.tum.`in`.tumcampusapp.component.ui.overview.MainActivity
import de.tum.`in`.tumcampusapp.component.ui.studyroom.StudyRoomsActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.EventsActivity
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.add
import de.tum.`in`.tumcampusapp.utils.allItems

class DrawerMenuHelper(private val activity: Activity) {

    private val drawerBundle: Bundle
        get() = Bundle().apply { putBoolean(Const.SHOW_DRAWER, true) }

    fun populateMenu(navigationView: NavigationView) {
        val hasTUMOAccess = AccessTokenManager.hasValidAccessToken(activity)
        val chatEnabled = Utils.getSettingBool(activity, Const.GROUP_CHAT_ENABLED, false)

        val allItems = mutableListOf<SideNavigationItem>()
        val navigationMenu = navigationView.menu

        navigationMenu.add(activity, HOME, drawerBundle)
        allItems.add(HOME)

        val myTumMenu = navigationMenu.addSubMenu(R.string.my_tum)
        if (hasTUMOAccess) {
            val items = MY_TUM.filterNot { it.needsChatAccess && !chatEnabled }
            items.forEach {
                myTumMenu.add(activity, it, drawerBundle)
                allItems.add(it)
            }
        }

        // General information which mostly can be used without a TUMonline token
        val commonTumMenu = navigationMenu.addSubMenu(R.string.common_info)
        COMMON_TUM
                .filterNot { it.needsTUMOAccess && !hasTUMOAccess }
                .forEach {
                    commonTumMenu.add(activity, it, drawerBundle)
                    allItems.add(it)
                }

        // App related menu entries
        val aboutMenu = navigationMenu.addSubMenu(R.string.about)
        ABOUT.forEach {
            aboutMenu.add(activity, it)
            allItems.add(it)
        }

        // Highlight the currently selected activity.
        val currentIndex = allItems
                .indexOfFirst { it.activity == activity::class.java }
                .takeIf { it != -1 }
        currentIndex?.let { index ->
            navigationMenu.allItems[index].apply {
                isCheckable = true
                isChecked = true
            }
        }
    }

    companion object {

        private val HOME = SideNavigationItem(R.string.home, R.drawable.ic_outline_home_24px, MainActivity::class.java)

        private val MY_TUM = arrayOf(
                SideNavigationItem(R.string.schedule, R.drawable.ic_outline_event_24px, CalendarActivity::class.java, true),
                SideNavigationItem(R.string.my_lectures, R.drawable.ic_outline_school_24px, LecturesPersonalActivity::class.java, true),
                SideNavigationItem(R.string.chat_rooms, R.drawable.ic_outline_chat_bubble_outline_24px, ChatRoomsActivity::class.java, true, true),
                SideNavigationItem(R.string.my_grades, R.drawable.ic_outline_insert_chart_outlined_24px, GradesActivity::class.java, true),
                SideNavigationItem(R.string.tuition_fees, R.drawable.ic_money, TuitionFeesActivity::class.java, true)
        )

        private val COMMON_TUM = arrayOf(
                SideNavigationItem(R.string.menues, R.drawable.ic_cutlery, CafeteriaActivity::class.java),
                SideNavigationItem(R.string.study_rooms, R.drawable.ic_outline_group_work_24px, StudyRoomsActivity::class.java),
                SideNavigationItem(R.string.roomfinder, R.drawable.ic_outline_location_on_24px, RoomFinderActivity::class.java),
                SideNavigationItem(R.string.person_search, R.drawable.ic_outline_people_outline_24px, PersonSearchActivity::class.java, true),
                SideNavigationItem(R.string.news, R.drawable.ic_rss, NewsActivity::class.java),
                SideNavigationItem(R.string.events_tickets, R.drawable.ic_outline_event_seat_24px, EventsActivity::class.java),
                SideNavigationItem(R.string.barrier_free, R.drawable.ic_outline_accessible_24px, BarrierFreeInfoActivity::class.java),
                SideNavigationItem(R.string.opening_hours, R.drawable.ic_outline_access_time_24px, OpeningHoursListActivity::class.java)
        )

        private val ABOUT = arrayOf(
                SideNavigationItem(R.string.show_feedback, R.drawable.ic_outline_feedback_24px, FeedbackActivity::class.java),
                SideNavigationItem(R.string.about_tca, R.drawable.ic_action_info_black, InformationActivity::class.java),
                SideNavigationItem(R.string.settings, R.drawable.ic_outline_settings_24px, UserPreferencesActivity::class.java)
        )

    }

}
