package de.tum.`in`.tumcampusapp.component.other.generic.drawer

import android.content.Context
import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.view.Menu
import android.view.MenuItem

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
import de.tum.`in`.tumcampusapp.component.ui.curricula.CurriculaActivity
import de.tum.`in`.tumcampusapp.component.ui.news.NewsActivity
import de.tum.`in`.tumcampusapp.component.ui.onboarding.WizNavStartActivity
import de.tum.`in`.tumcampusapp.component.ui.openinghour.OpeningHoursListActivity
import de.tum.`in`.tumcampusapp.component.ui.overview.InformationActivity
import de.tum.`in`.tumcampusapp.component.ui.plan.PlansActivity
import de.tum.`in`.tumcampusapp.component.ui.studyroom.StudyRoomsActivity
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportationActivity
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils

class DrawerMenuHelper(private val mContext: Context, private val mDrawerLayout: DrawerLayout) : NavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        mDrawerLayout.closeDrawers()
        val intent = menuItem.intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        mContext.startActivity(intent)
        return true
    }

    fun populateMenu(navigationMenu: Menu) {
        val hasTUMOAccess = AccessTokenManager(mContext).hasValidAccessToken()
        val chatEnabled = Utils.getSettingBool(mContext, Const.GROUP_CHAT_ENABLED, false)

        val myTumMenu = navigationMenu.addSubMenu(R.string.my_tum)
        if (hasTUMOAccess) {
            for (item in MY_TUM) {
                if (!(item.needsChatAccess && !chatEnabled)) {
                    myTumMenu.add(item.titleRes)
                            .setIcon(item.iconRes).intent = Intent(mContext, item.activity)
                }
            }
        } else {
            myTumMenu.add(R.string.tumonline_login)
                    .setIcon(R.drawable.ic_link).intent = Intent(mContext, WizNavStartActivity::class.java)
        }

        // General information which mostly can be used without a TUMonline token
        val commonTumMenu = navigationMenu.addSubMenu(R.string.common_info)
        for (item in COMMON_TUM) {
            if (!(item.needsTUMOAccess && !hasTUMOAccess)) {
                commonTumMenu.add(item.titleRes)
                        .setIcon(item.iconRes).intent = Intent(mContext, item.activity)
            }
        }

        // App related menu entries
        for (item in APP) {
            navigationMenu.add(item.titleRes)
                    .setIcon(item.iconRes).intent = Intent(mContext, item.activity)
        }
    }


    companion object {
        private val MY_TUM = arrayOf(
                SideNavigationItem(R.string.schedule, R.drawable.ic_calendar, CalendarActivity::class.java, true, false),
                SideNavigationItem(R.string.my_lectures, R.drawable.ic_my_lectures, LecturesPersonalActivity::class.java, true, false),
                SideNavigationItem(R.string.chat_rooms, R.drawable.ic_comment, ChatRoomsActivity::class.java, true, true),
                SideNavigationItem(R.string.my_grades, R.drawable.ic_my_grades, GradesActivity::class.java, true, false),
                SideNavigationItem(R.string.tuition_fees, R.drawable.ic_money, TuitionFeesActivity::class.java, true, false)
        )

        private val COMMON_TUM = arrayOf(
                SideNavigationItem(R.string.menues, R.drawable.ic_cutlery, CafeteriaActivity::class.java, false, false),
                SideNavigationItem(R.string.study_rooms, R.drawable.ic_group_work, StudyRoomsActivity::class.java, false, false),
                SideNavigationItem(R.string.roomfinder, R.drawable.ic_place, RoomFinderActivity::class.java, false, false),
                SideNavigationItem(R.string.plans, R.drawable.ic_plans, PlansActivity::class.java, false, false),
                SideNavigationItem(R.string.mvv, R.drawable.ic_mvv, TransportationActivity::class.java, false, false),
                SideNavigationItem(R.string.person_search, R.drawable.ic_users, PersonSearchActivity::class.java, true, false),
                SideNavigationItem(R.string.news, R.drawable.ic_rss, NewsActivity::class.java, false, false),
                SideNavigationItem(R.string.barrier_free, R.drawable.ic_accessible, BarrierFreeInfoActivity::class.java, false, false),
                SideNavigationItem(R.string.opening_hours, R.drawable.ic_time, OpeningHoursListActivity::class.java, false, false),
                SideNavigationItem(R.string.study_plans, R.drawable.ic_study_plans, CurriculaActivity::class.java, false, false)
        )

        private val APP = arrayOf(
                SideNavigationItem(R.string.show_feedback, R.drawable.ic_feedback, FeedbackActivity::class.java, false, false),
                SideNavigationItem(R.string.about_tca, R.drawable.ic_action_info_black, InformationActivity::class.java, false, false),
                SideNavigationItem(R.string.settings, R.drawable.ic_action_settings, UserPreferencesActivity::class.java, false, false)
        )
    }
}
