package de.tum.`in`.tumcampusapp.component.other.generic.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.drawer.DrawerHeaderInflater
import de.tum.`in`.tumcampusapp.component.other.generic.drawer.DrawerMenuHelper
import de.tum.`in`.tumcampusapp.component.other.navigation.NavAction
import de.tum.`in`.tumcampusapp.component.other.navigation.NavManager
import de.tum.`in`.tumcampusapp.component.ui.overview.MainFragment
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.closeDrawers
import org.jetbrains.anko.defaultSharedPreferences

class BaseNavigationActivity : BaseActivity(
    R.layout.activity_main
), SharedPreferences.OnSharedPreferenceChangeListener {

    private val navAction: NavAction? by lazy {
        intent.getParcelableExtra<NavAction?>(KEY_NAV_ACTION)
    }

    private val drawerHeaderInflater: DrawerHeaderInflater by lazy {
        DrawerHeaderInflater(this)
    }

    private val drawerLayout: DrawerLayout by lazy {
        findViewById<DrawerLayout>(R.id.drawer_layout)
    }

    private val navigationView: NavigationView by lazy {
        findViewById<NavigationView>(R.id.left_drawer)
    }

    private val drawerMenuHelper: DrawerMenuHelper by lazy {
        DrawerMenuHelper(this, navigationView)
    }

    private var drawerToggle: ActionBarDrawerToggle? = null

    private val fragmentCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            initDrawerToggle()
            updateDrawer()
        }
    }

    private val shortcutDestination: NavAction? by lazy {
        NavAction.fromIntent(intent)
    }

    private val navManager: NavManager by lazy {
        NavManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initDrawer()
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentCallbacks, false)
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentFrame, MainFragment.newInstance())
                .commit()

            navAction?.let {
                navManager.openFragment(it.createDestination())
            }

            shortcutDestination?.let {
                navManager.openFragment(it.createDestination())
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle?.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle?.onConfigurationChanged(newConfig)
    }

    private fun initDrawer() {
        drawerHeaderInflater.inflater(navigationView)
        drawerMenuHelper.populateMenu()

        navigationView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawers {
                val navItem = drawerMenuHelper.findNavItem(item)
                navManager.open(navItem)
            }
            true
        }
    }

    private fun initDrawerToggle() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)

        drawerToggle = object : ActionBarDrawerToggle(this, drawerLayout,
            toolbar, R.string.drawer_open, R.string.drawer_close) {}

        supportActionBar?.let {
            enableDrawer(true)
            drawerToggle?.isDrawerIndicatorEnabled = true
        }

        drawerToggle?.let {
            it.syncState()
            drawerLayout?.addDrawerListener(it)
        }
    }

    private fun updateDrawer() {
        drawerMenuHelper.updateNavDrawer()
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String
    ) {
        if (key == Const.EMPLOYEE_MODE) {
            drawerMenuHelper.populateMenu()
        }
    }

    fun enableDrawer(isEnabled: Boolean) {
        val mode = if (isEnabled) LOCK_MODE_UNLOCKED else LOCK_MODE_LOCKED_CLOSED
        drawerLayout.setDrawerLockMode(mode)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val handled = drawerToggle?.onOptionsItemSelected(item) ?: false
        return handled || super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    companion object {

        private const val KEY_NAV_ACTION = "KEY_NAV_ACTION"

        private fun newNavActionIntent(
            context: Context,
            navAction: NavAction
        ): Intent {
            return newIntent(context).putExtra(KEY_NAV_ACTION, navAction)
        }

        @JvmStatic
        fun newIntent(
            context: Context
        ): Intent {
            return Intent(context, BaseNavigationActivity::class.java)
        }

        @JvmStatic
        fun newCafeteriaIntent(
            context: Context,
            cafeteriaId: Int
        ): Intent {
            return newNavActionIntent(context, NavAction.Cafeteria(cafeteriaId))
        }

        @JvmStatic
        fun newChatIntent(
            context: Context
        ): Intent {
            return newNavActionIntent(context, NavAction.Chat)
        }

        @JvmStatic
        fun newNewsIntent(
            context: Context
        ): Intent {
            return newNavActionIntent(context, NavAction.News)
        }

        @JvmStatic
        fun newEventsIntent(
            context: Context
        ): Intent {
            return newNavActionIntent(context, NavAction.Events)
        }

        @JvmStatic
        fun newTuitionIntent(
            context: Context
        ): Intent {
            return newNavActionIntent(context, NavAction.Tuition)
        }

        @JvmStatic
        fun newRoomFinderIntent(
            context: Context,
            roomNumber: String
        ): Intent {
            return newNavActionIntent(context, NavAction.RoomFinder(roomNumber))
        }

        @JvmStatic
        fun newGradesIntent(
            context: Context
        ): Intent {
            return newNavActionIntent(context, NavAction.Grades)
        }

        @JvmStatic
        fun newCalendarIntent(
            context: Context
        ): Intent {
            return newNavActionIntent(context, NavAction.Calendar)
        }

    }

}
