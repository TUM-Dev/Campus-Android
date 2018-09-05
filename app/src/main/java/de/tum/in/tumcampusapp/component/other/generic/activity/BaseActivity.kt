package de.tum.`in`.tumcampusapp.component.other.generic.activity

import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.NavUtils
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.drawer.DrawerMenuHelper
import de.tum.`in`.tumcampusapp.component.other.navigation.NavigationManager
import de.tum.`in`.tumcampusapp.component.ui.overview.MainActivity
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import java.util.*

/**
 * Takes care of the navigation drawer which might be attached to the activity and also handles up navigation
 */
abstract class BaseActivity(private val layoutId: Int) : AppCompatActivity() {

    private val toolbar: Toolbar by lazy { findViewById<Toolbar>(R.id.main_toolbar) }

    private val drawerLayout: DrawerLayout? by lazy {
        findViewById<DrawerLayout>(R.id.drawer_layout)
    }

    private val drawerList: NavigationView? by lazy {
        findViewById<NavigationView>(R.id.left_drawer)
    }

    private var drawerToggle: ActionBarDrawerToggle? = null

    private val shouldShowDrawer: Boolean
        get() {
            val askedToShowDrawer = intent.getBooleanExtra(Const.SHOW_DRAWER, false)
            return drawerLayout != null && (askedToShowDrawer || this is MainActivity)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        setUpToolbar()
        setUpDrawer()
    }

    open fun setUpToolbar() {
        setSupportActionBar(toolbar)

        supportActionBar?.let {
            val parent = NavUtils.getParentActivityName(this)
            if (parent != null || this is MainActivity) {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeButtonEnabled(true)
            }
        }
    }

    private fun setUpDrawer() {
        drawerToggle = object : ActionBarDrawerToggle(
                this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                this@BaseActivity.invalidateOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                this@BaseActivity.invalidateOptionsMenu()
            }
        }

        supportActionBar?.let {
            val mode = if (shouldShowDrawer) {
                DrawerLayout.LOCK_MODE_UNLOCKED
            } else {
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            }

            drawerLayout?.setDrawerLockMode(mode)
            drawerToggle?.isDrawerIndicatorEnabled = shouldShowDrawer
        }

        if (!shouldShowDrawer) {
            return
        }

        drawerList?.let {
            val headerView = it.inflateHeaderView(R.layout.drawer_header)
            headerView?.let { view -> setupDrawerHeader(view) }

            val helper = DrawerMenuHelper(this)
            helper.populateMenu(it)

            it.setNavigationItemSelectedListener { item ->
                drawerLayout?.addDrawerListener(object : DrawerLayout.DrawerListener {
                    override fun onDrawerStateChanged(newState: Int) = Unit

                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit

                    override fun onDrawerOpened(drawerView: View) = Unit

                    override fun onDrawerClosed(drawerView: View) {
                        NavigationManager.open(this@BaseActivity, item)
                    }
                })

                drawerLayout?.closeDrawer(Gravity.START)
                true
            }
        }

        drawerToggle?.let {
            it.syncState()
            drawerLayout?.addDrawerListener(it)
        }
    }

    private fun setupDrawerHeader(headerView: View) {
        val nameText = headerView.findViewById<TextView>(R.id.nameTextView)
        val emailText = headerView.findViewById<TextView>(R.id.emailTextView)

        nameText.text = Utils.getSetting(this,
                Const.CHAT_ROOM_DISPLAY_NAME, getString(R.string.not_connected_to_tumonline))

        val lrzId = Utils.getSetting(this, Const.LRZ_ID, "")
        val email = if (lrzId.isNotEmpty()) "$lrzId@mytum.de" else ""
        if (email.isNotEmpty()) {
            emailText.text = email
        } else {
            emailText.visibility = View.GONE
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

    fun openDrawer() {
        drawerLayout?.openDrawer(Gravity.START)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val handled = drawerToggle?.onOptionsItemSelected(item) ?: false
        if (shouldShowDrawer && handled) {
            return true
        }

        if (item.itemId == android.R.id.home) {
            NavigationManager.closeActivity(this)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (shouldShowDrawer) {
            drawerToggle?.syncState()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (shouldShowDrawer) {
            drawerToggle?.onConfigurationChanged(newConfig)
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
        Picasso.get().load(url).into(imageView)
    }

    override fun onBackPressed() {
        val handled = NavigationManager.onBackPressed(this)
        if (!handled) {
            super.onBackPressed()
        }
    }

}
