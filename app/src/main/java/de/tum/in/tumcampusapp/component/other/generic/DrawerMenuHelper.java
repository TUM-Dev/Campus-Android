package de.tum.in.tumcampusapp.component.other.generic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.AccessTokenManager;
import de.tum.in.tumcampusapp.component.other.settings.UserPreferencesActivity;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarActivity;
import de.tum.in.tumcampusapp.component.tumui.grades.GradesActivity;
import de.tum.in.tumcampusapp.component.tumui.lectures.activity.LecturesPersonalActivity;
import de.tum.in.tumcampusapp.component.tumui.person.PersonsSearchActivity;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.TuitionFeesActivity;
import de.tum.in.tumcampusapp.component.ui.barrierfree.BarrierFreeInfoActivity;
import de.tum.in.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity;
import de.tum.in.tumcampusapp.component.ui.chat.activity.ChatRoomsActivity;
import de.tum.in.tumcampusapp.component.ui.curricula.CurriculaActivity;
import de.tum.in.tumcampusapp.component.ui.news.NewsActivity;
import de.tum.in.tumcampusapp.component.ui.openinghour.OpeningHoursListActivity;
import de.tum.in.tumcampusapp.component.ui.plan.PlansActivity;
import de.tum.in.tumcampusapp.component.ui.studyroom.StudyRoomsActivity;
import de.tum.in.tumcampusapp.component.ui.transportation.TransportationActivity;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

public class DrawerMenuHelper implements NavigationView.OnNavigationItemSelectedListener {

    private static final SideNavigationItem[] MY_TUM = {
            new SideNavigationItem(R.string.schedule, R.drawable.ic_calendar, CalendarActivity.class, true, false),
            new SideNavigationItem(R.string.my_lectures, R.drawable.ic_my_lectures, LecturesPersonalActivity.class, true, false),
            new SideNavigationItem(R.string.chat_rooms, R.drawable.ic_comment, ChatRoomsActivity.class, true, true),
            new SideNavigationItem(R.string.my_grades, R.drawable.ic_my_grades, GradesActivity.class, true, false),
            new SideNavigationItem(R.string.tuition_fees, R.drawable.ic_money, TuitionFeesActivity.class, true, false),
            };

    private static final SideNavigationItem[] COMMON_TUM = {
            new SideNavigationItem(R.string.menues, R.drawable.ic_cutlery, CafeteriaActivity.class, false, false),
            new SideNavigationItem(R.string.study_rooms, R.drawable.ic_group_work, StudyRoomsActivity.class, false, false),
            new SideNavigationItem(R.string.roomfinder, R.drawable.ic_place, RoomFinderActivity.class, false, false),
            new SideNavigationItem(R.string.plans, R.drawable.ic_plans, PlansActivity.class, false, false),
            new SideNavigationItem(R.string.mvv, R.drawable.ic_mvv, TransportationActivity.class, false, false),
            new SideNavigationItem(R.string.person_search, R.drawable.ic_users, PersonsSearchActivity.class, true, false),
            new SideNavigationItem(R.string.news, R.drawable.ic_rss, NewsActivity.class, false, false),
            new SideNavigationItem(R.string.barrier_free, R.drawable.ic_accessible, BarrierFreeInfoActivity.class, false, false),
            new SideNavigationItem(R.string.opening_hours, R.drawable.ic_time, OpeningHoursListActivity.class, false, false),
            new SideNavigationItem(R.string.study_plans, R.drawable.ic_study_plans, CurriculaActivity.class, false, false)

    };

    private static final SideNavigationItem[] APP = {
            new SideNavigationItem(R.string.settings, R.drawable.ic_action_settings, UserPreferencesActivity.class, false, false)
    };

    private final Context mContext;

    private final DrawerLayout mDrawerLayout;

    public DrawerMenuHelper(Context context, DrawerLayout drawerLayout) {
        mContext = context;
        mDrawerLayout = drawerLayout;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawers();
        Intent intent = menuItem.getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mContext.startActivity(intent);
        return true;
    }

    public void populateMenu(Menu navigationMenu) {
        boolean hasTUMOAccess = new AccessTokenManager(mContext).hasValidAccessToken();
        boolean chatEnabled = Utils.getSettingBool(mContext, Const.GROUP_CHAT_ENABLED, false);

        if (hasTUMOAccess) {
            SubMenu myTumMenu = navigationMenu.addSubMenu(R.string.my_tum);
            for (SideNavigationItem item : MY_TUM) {
                if (!(item.needsChatAccess && !chatEnabled)) {
                    myTumMenu.add(item.titleRes)
                             .setIcon(item.iconRes)
                             .setIntent(new Intent(mContext, item.activity));
                }
            }
        }

        SubMenu commonTumMenu = navigationMenu.addSubMenu(R.string.tum_common);
        for (SideNavigationItem item : COMMON_TUM) {
            if (!(item.needsTUMOAccess && !hasTUMOAccess)) {
                commonTumMenu.add(item.titleRes)
                             .setIcon(item.iconRes)
                             .setIntent(new Intent(mContext, item.activity));
            }
        }
        for (SideNavigationItem item : APP) {
            navigationMenu.add(item.titleRes)
                          .setIcon(item.iconRes)
                          .setIntent(new Intent(mContext, item.activity));
        }
    }

    private static class SideNavigationItem {
        final int titleRes;
        final int iconRes;
        final Class<? extends Activity> activity;
        final boolean needsTUMOAccess;
        final boolean needsChatAccess;

        SideNavigationItem(int titleRes, int iconRes, Class<? extends Activity> activity, boolean needsTUMOAccess, boolean needsChatAccess) {
            this.titleRes = titleRes;
            this.iconRes = iconRes;
            this.activity = activity;
            this.needsTUMOAccess = needsTUMOAccess;
            this.needsChatAccess = needsChatAccess;
        }
    }
}
