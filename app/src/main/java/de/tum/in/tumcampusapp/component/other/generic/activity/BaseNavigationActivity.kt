package de.tum.`in`.tumcampusapp.component.other.generic.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.other.generic.drawer.DrawerMenuHelper
import de.tum.`in`.tumcampusapp.component.ui.onboarding.WizNavStartActivity
import de.tum.`in`.tumcampusapp.component.ui.overview.MainFragment
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.closeDrawers
import org.jetbrains.anko.defaultSharedPreferences
import java.util.Locale

class BaseNavigationActivity : BaseActivity(
    R.layout.activity_main
), SharedPreferences.OnSharedPreferenceChangeListener {

    private val drawerLayout: DrawerLayout by lazy {
        findViewById<DrawerLayout>(R.id.drawer_layout)
    }

    private val drawerList: NavigationView by lazy {
        findViewById<NavigationView>(R.id.left_drawer)
    }

    private val drawerMenuHelper: DrawerMenuHelper by lazy {
        DrawerMenuHelper(this)
    }

    private var drawerToggle: ActionBarDrawerToggle? = null

    private val callbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            initDrawerToggle()
            drawerMenuHelper.updateNavDrawer(drawerList)
        }
    }

    private fun initDrawerToggle() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)

        drawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {}

        supportActionBar?.let {
            enableDrawer(true)
            drawerToggle?.isDrawerIndicatorEnabled = true
        }

        drawerToggle?.let {
            it.syncState()
            drawerLayout?.addDrawerListener(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initDrawer()
        supportFragmentManager.registerFragmentLifecycleCallbacks(callbacks, false)
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentFrame, MainFragment.newInstance())
                .commit()
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
        val headerView = drawerList.inflateHeaderView(R.layout.drawer_header)
        setupDrawerHeader(headerView)

        drawerMenuHelper.populateMenu(drawerList)

        drawerList.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawers { openNavigationItem(item) }
            true
        }
    }

    private fun openNavigationItem(item: MenuItem) {
        // NavigationManager.open(this, item)
        drawerMenuHelper.open(item)
    }

    private fun setupDrawerHeader(headerView: View) {
        val background = headerView.findViewById<ImageView>(R.id.background)
        val nameTextView = headerView.findViewById<TextView>(R.id.nameTextView)
        val emailTextView = headerView.findViewById<TextView>(R.id.emailTextView)
        val loginButton = headerView.findViewById<MaterialButton>(R.id.loginButton)

        val isLoggedIn = AccessTokenManager.hasValidAccessToken(this)
        background.visibility = if (isLoggedIn) View.VISIBLE else View.GONE

        if (isLoggedIn) {
            val name = Utils.getSetting(this, Const.CHAT_ROOM_DISPLAY_NAME, "")
            if (name.isNotEmpty()) {
                nameTextView.text = name
            } else {
                nameTextView.visibility = View.INVISIBLE
            }

            val lrzId = Utils.getSetting(this, Const.LRZ_ID, "")
            val email = if (lrzId.isNotEmpty()) "$lrzId@mytum.de" else ""
            if (email.isNotEmpty()) {
                emailTextView.text = email
            } else {
                emailTextView.visibility = View.GONE
            }

            loginButton.visibility = View.GONE
        } else {
            nameTextView.visibility = View.GONE
            emailTextView.visibility = View.GONE

            loginButton.visibility = View.VISIBLE
            loginButton.setOnClickListener {
                val intent = Intent(this, WizNavStartActivity::class.java)
                startActivity(intent)
            }
        }

        fetchProfilePicture(headerView)

        val divider = headerView.findViewById<View>(R.id.divider)
        val rainbowBar = headerView.findViewById<View>(R.id.rainbow_bar)

        if (Utils.getSettingBool(this, Const.RAINBOW_MODE, false)) {
            divider.visibility = View.GONE
            rainbowBar.visibility = View.VISIBLE
        } else {
            divider.visibility = View.VISIBLE
            rainbowBar.visibility = View.GONE
        }
    }

    private fun fetchProfilePicture(headerView: View) {
        val id = Utils.getSetting(this, Const.TUMO_PIDENT_NR, "")
        val parts = id.split("\\*".toRegex()).toTypedArray()
        if (parts.size != 2) {
            return
        }

        val group = parts[0]
        val personId = parts[1]
        val url = String.format(Locale.getDefault(),
            Const.TUM_ONLINE_PROFILE_PICTURE_URL_FORMAT_STRING, group, personId)

        val imageView = headerView.findViewById<CircleImageView>(R.id.profileImageView)
        Picasso.get()
            .load(url)
            .error(R.drawable.photo_not_available)
            .placeholder(R.drawable.photo_not_available)
            .into(imageView)
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String
    ) {
        if (key == Const.EMPLOYEE_MODE) {
            // Update the drawer contents (not the header)
            drawerList.menu?.clear()
            drawerMenuHelper.populateMenu(drawerList)
        }
    }

    fun enableDrawer(isEnabled: Boolean) {
        val mode = if (isEnabled) {
            DrawerLayout.LOCK_MODE_UNLOCKED
        } else {
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        }

        drawerLayout.setDrawerLockMode(mode)
    }

    @SuppressLint("WrongConstant")
    fun openDrawer() {
        drawerLayout.openDrawer(Gravity.START)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val handled = drawerToggle?.onOptionsItemSelected(item) ?: false
        return handled || super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

}
