package de.tum.`in`.tumcampusapp.component.other.generic.drawer

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.other.navigation.NavigationManager
import de.tum.`in`.tumcampusapp.component.other.settings.UserPreferencesActivity
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarFragment
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackActivity
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradesFragment
import de.tum.`in`.tumcampusapp.component.tumui.lectures.fragment.LecturesFragment
import de.tum.`in`.tumcampusapp.component.tumui.person.PersonSearchFragment
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderFragment
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeesFragment
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.BarrierFreeInfoFragment
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.fragment.CafeteriaFragment
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatRoomsFragment
import de.tum.`in`.tumcampusapp.component.ui.news.NewsFragment
import de.tum.`in`.tumcampusapp.component.ui.openinghour.OpeningHoursListFragment
import de.tum.`in`.tumcampusapp.component.ui.overview.InformationActivity
import de.tum.`in`.tumcampusapp.component.ui.overview.MainFragment
import de.tum.`in`.tumcampusapp.component.ui.studyroom.StudyRoomsFragment
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.EventsFragment
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.add
import de.tum.`in`.tumcampusapp.utils.allItems

class DrawerMenuHelper(private val activity: AppCompatActivity) {

    private val currentFragment: Fragment?
        get() = activity.supportFragmentManager.findFragmentById(R.id.contentFrame)

    private val allItems = mutableListOf<NavItem>()

    fun populateMenu(navigationView: NavigationView) {
        val hasTUMOAccess = AccessTokenManager.hasValidAccessToken(activity)
        val chatEnabled = Utils.getSettingBool(activity, Const.GROUP_CHAT_ENABLED, false)
        val employeeMode = Utils.getSettingBool(activity, Const.EMPLOYEE_MODE, false)

        val navigationMenu = navigationView.menu
        allItems.clear()

        navigationMenu.add(HOME)
        allItems.add(HOME)

        val myTumMenu = navigationMenu.addSubMenu(R.string.my_tum)
        if (hasTUMOAccess) {
            var items = MY_TUM.filterNot {
                it.needsChatAccess && !chatEnabled }
            if (employeeMode) {
                items = items.filterNot { it.hideForEmployees }
            }
            items.forEach {
                myTumMenu.add(it)
                allItems.add(it)
            }
        }

        // General information which mostly can be used without a TUMonline token
        val commonTumMenu = navigationMenu.addSubMenu(R.string.common_info)
        COMMON_TUM
                .filterNot { it.needsTUMOAccess && !hasTUMOAccess }
                .forEach {
                    commonTumMenu.add(it)
                    allItems.add(it)
                }

        // App related menu entries
        val aboutMenu = navigationMenu.addSubMenu(R.string.about)
        ABOUT.forEach {
            aboutMenu.add(it)
            allItems.add(it)
        }

        // Highlight the currently selected activity.
        val currentIndex = allItems
            .mapNotNull { it as? NavItem.FragmentDestination }
            .indexOfFirst { it.fragment == currentFragment?.javaClass }
            .takeIf { it != -1 }

        currentIndex?.let { index ->
            navigationMenu.allItems[index].apply {
                isCheckable = true
                isChecked = true
            }
        }
    }

    fun updateNavDrawer(navigationView: NavigationView) {
        val navigationMenu = navigationView.menu
        navigationMenu.allItems.forEach {
            it.isChecked = false
        }

        // Highlight the currently selected activity.
        val currentIndex = allItems
            .mapNotNull { it as? NavItem.FragmentDestination }
            .indexOfFirst { it.fragment == currentFragment?.javaClass }
            .takeIf { it != -1 }

        currentIndex?.let { index ->
            navigationMenu.allItems[index].apply {
                isCheckable = true
                isChecked = true
            }
        }
    }

    fun open(menuItem: MenuItem) {
        if (menuItem.title == activity.getString(HOME.titleRes)) {
            NavigationManager.popBackToHome(activity)
            return
        }

        for (item in MY_TUM + COMMON_TUM) {
            if (menuItem.title == activity.getString(item.titleRes)) {
                NavigationManager.open(activity, item)
                return
            }
        }

        for (item in ABOUT) {
            if (menuItem.title == activity.getString(item.titleRes)) {
                NavigationManager.open(activity, item)
                return
            }
        }
    }

    companion object {

        private val HOME = NavItem.FragmentDestination(R.string.home, R.drawable.ic_outline_home_24px, MainFragment::class.java)

        private val MY_TUM = arrayOf(
                NavItem.FragmentDestination(R.string.calendar, R.drawable.ic_outline_event_24px, CalendarFragment::class.java, true),
                NavItem.FragmentDestination(R.string.my_lectures, R.drawable.ic_outline_school_24px, LecturesFragment::class.java, true, hideForEmployees = true),
                NavItem.FragmentDestination(R.string.chat_rooms, R.drawable.ic_outline_chat_bubble_outline_24px, ChatRoomsFragment::class.java, true, true),
                NavItem.FragmentDestination(R.string.my_grades, R.drawable.ic_outline_insert_chart_outlined_24px, GradesFragment::class.java, true, hideForEmployees = true),
                NavItem.FragmentDestination(R.string.tuition_fees, R.drawable.ic_money, TuitionFeesFragment::class.java, true, hideForEmployees = true)
        )

        private val COMMON_TUM = arrayOf(
                NavItem.FragmentDestination(R.string.menues, R.drawable.ic_cutlery, CafeteriaFragment::class.java),
                NavItem.FragmentDestination(R.string.study_rooms, R.drawable.ic_outline_group_work_24px, StudyRoomsFragment::class.java),
                NavItem.FragmentDestination(R.string.roomfinder, R.drawable.ic_outline_location_on_24px, RoomFinderFragment::class.java),
                NavItem.FragmentDestination(R.string.person_search, R.drawable.ic_outline_people_outline_24px, PersonSearchFragment::class.java, true),
                NavItem.FragmentDestination(R.string.news, R.drawable.ic_rss, NewsFragment::class.java),
                NavItem.FragmentDestination(R.string.events_tickets, R.drawable.tickets, EventsFragment::class.java),
                NavItem.FragmentDestination(R.string.barrier_free, R.drawable.ic_outline_accessible_24px, BarrierFreeInfoFragment::class.java),
                NavItem.FragmentDestination(R.string.opening_hours, R.drawable.ic_outline_access_time_24px, OpeningHoursListFragment::class.java)
        )

        private val ABOUT = arrayOf(
                NavItem.ActivityDestination(R.string.show_feedback, R.drawable.ic_outline_feedback_24px, FeedbackActivity::class.java),
                NavItem.ActivityDestination(R.string.about_tca, R.drawable.ic_action_info_black, InformationActivity::class.java),
                NavItem.ActivityDestination(R.string.settings, R.drawable.ic_outline_settings_24px, UserPreferencesActivity::class.java)
        )

    }

}
