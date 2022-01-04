package de.tum.`in`.tumcampusapp.component.other.settings

import android.Manifest.permission.READ_CALENDAR
import android.Manifest.permission.WRITE_CALENDAR
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.squareup.picasso.Picasso
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.eduroam.SetupEduroamActivity
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.onboarding.StartupActivity
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.di.injector
import de.tum.`in`.tumcampusapp.service.SilenceService
import de.tum.`in`.tumcampusapp.service.StartSyncReceiver
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.notificationManager
import java.util.concurrent.ExecutionException
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private val themeProvider by lazy { ThemeProvider(requireContext()) }

    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var cafeteriaLocalRepository: CafeteriaLocalRepository

    @Inject
    lateinit var newsController: NewsController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.inject(this)
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        populateNewsSources()
        setUpEmployeeSettings()

        // Disables silence service and logout if the app is used without TUMOnline access
        val silentSwitch = findPreference(Const.SILENCE_SERVICE) as? SwitchPreferenceCompat
        val logoutButton = findPreference(BUTTON_LOGOUT)
        if (!AccessTokenManager.hasValidAccessToken(context)) {
            if (silentSwitch != null) {
                silentSwitch.isEnabled = false
            }
            logoutButton?.isVisible = false
        }

        // Only do these things if we are in the root of the preferences
        if (rootKey == null) {
            // Click listener for preference list entries. Used to simulate a button
            // (since it is not possible to add a button to the preferences screen)
            findPreference(BUTTON_LOGOUT).onPreferenceClickListener = this
            setSummary("language_preference")
            setSummary(Const.DESIGN_THEME)
            setSummary("card_default_campus")
            setSummary("silent_mode_set_to")
            setSummary("background_mode_set_to")
        } else if (rootKey == "card_cafeteria") {
            setSummary("card_cafeteria_default_G")
            setSummary("card_cafeteria_default_K")
            setSummary("card_cafeteria_default_W")
            setSummary("card_role")
            initCafeteriaCardSelections()
        } else if (rootKey == "card_mvv") {
            setSummary("card_stations_default_G")
            setSummary("card_stations_default_C")
            setSummary("card_stations_default_K")
        } else if (rootKey == "card_eduroam") {
            findPreference(SETUP_EDUROAM).onPreferenceClickListener = this
        }

        requireContext().defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun populateNewsSources() {
        val newsSourcesPreference = findPreference("card_news_sources") as? PreferenceCategory ?: return
        val newsSources = newsController.newsSources

        for (newsSource in newsSources) {
            val pref = CheckBoxPreference(requireContext())
            pref.key = "card_news_source_" + newsSource.id
            pref.setDefaultValue(true)

            // Reserve space so that when the icon is loaded the text is not moved again
            pref.isIconSpaceReserved = true

            // Load news source icon in background and set it
            val url = newsSource.icon
            if (url.isNotBlank()) {
                loadNewsSourceIcon(pref, url)
            }

            pref.title = newsSource.title
            newsSourcesPreference.addPreference(pref)
        }
    }

    private fun loadNewsSourceIcon(
        preference: Preference,
        url: String
    ) {
        compositeDisposable += Single
                .fromCallable { Picasso.get().load(url).get() }
                .subscribeOn(Schedulers.io())
                .map { BitmapDrawable(resources, it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(preference::setIcon, Utils::log)
    }

    /**
     * Disable setting for non-employees.
     */
    private fun setUpEmployeeSettings() {
        val isEmployee = Utils.getSetting(requireContext(), Const.TUMO_EMPLOYEE_ID, "").isNotEmpty()
        val checkbox = findPreference(Const.EMPLOYEE_MODE) ?: return
        if (!isEmployee) {
            checkbox.isEnabled = false
        }
    }

    override fun onSharedPreferenceChanged(
        sharedPrefs: SharedPreferences,
        key: String?
    ) {
        if (key == null) {
            return
        }
        setSummary(key)

        // When newspread selection changes
        // deselect all newspread sources and select only the
        // selected source if one of all was selected before
        if (key == "news_newspread") {
            val newspreadIds = 7..13
            val value = newspreadIds.any { sharedPrefs.getBoolean("card_news_source_$it", false) }
            val newSource = sharedPrefs.getString(key, "7")

            sharedPrefs.edit {
                newspreadIds.forEach { putBoolean("card_news_source_$it", false) }
                putBoolean("card_news_source_$newSource", value)
            }
        }

        // If the silent mode was activated, start the service. This will invoke
        // the service to call onHandleIntent which checks available lectures
        if (key == Const.SILENCE_SERVICE) {
            val service = Intent(requireContext(), SilenceService::class.java)
            if (sharedPrefs.getBoolean(key, false)) {
                if (!SilenceService.hasPermissions(requireContext())) {
                    // disable until silence service permission is resolved
                    val silenceSwitch = findPreference(Const.SILENCE_SERVICE) as SwitchPreferenceCompat
                    silenceSwitch.isChecked = false
                    Utils.setSetting(requireContext(), Const.SILENCE_SERVICE, false)
                    SilenceService.requestPermissions(requireContext())
                } else {
                    requireContext().startService(service)
                }
            } else {
                requireContext().stopService(service)
            }
        }

        // Change design theme
        if (key == Const.DESIGN_THEME) {
            val theme = themeProvider.getTheme(sharedPrefs.getString(key, "system")!!)
            AppCompatDelegate.setDefaultNightMode(theme)
        }

        // If the background mode was activated, start the service. This will invoke
        // the service to call onHandleIntent which updates all background data
        if (key == Const.BACKGROUND_MODE) {
            if (sharedPrefs.getBoolean(key, false)) {
                StartSyncReceiver.startBackground(requireContext())
            } else {
                StartSyncReceiver.cancelBackground()
            }
        }

        // restart app after language change
        if (key == "language_preference" && activity != null) {
            (activity as SettingsActivity).restartApp()
        }
    }

    private fun initCafeteriaCardSelections() {
        val cafeterias = cafeteriaLocalRepository
                .getAllCafeterias()
                .blockingFirst()
                .sortedBy { it.name }

        val cafeteriaByLocationName = getString(R.string.settings_cafeteria_depending_on_location)
        val cafeteriaNames = listOf(cafeteriaByLocationName) + cafeterias.map { it.name }

        val cafeteriaByLocationId = Const.CAFETERIA_BY_LOCATION_SETTINGS_ID
        val cafeteriaIds = listOf(cafeteriaByLocationId) + cafeterias.map { it.id.toString() }

        val preference = findPreference(Const.CAFETERIA_CARDS_SETTING) as MultiSelectListPreference
        preference.entries = cafeteriaNames.toTypedArray()
        preference.entryValues = cafeteriaIds.toTypedArray()

        preference.setOnPreferenceChangeListener { pref, newValue ->
            (pref as MultiSelectListPreference).values = newValue as Set<String>
            setCafeteriaCardsSummary(preference)
            false
        }

        setCafeteriaCardsSummary(preference)
    }

    private fun setSummary(key: CharSequence) {
        val pref = findPreference(key)
        if (pref is ListPreference) {
            val entry = pref.entry.toString()
            pref.summary = entry
        }
    }

    private fun setCafeteriaCardsSummary(preference: MultiSelectListPreference) {
        val values = preference.values
        if (values.isEmpty()) {
            preference.setSummary(R.string.settings_no_location_selected)
        } else {
            preference.summary = values
                    .map { preference.findIndexOfValue(it) }
                    .map { preference.entries[it] }
                    .map { it.toString() }
                    .sorted()
                    .joinToString(", ")
        }
    }

    override fun onPreferenceClick(
        preference: Preference?
    ): Boolean {
        when (preference?.key) {
            SETUP_EDUROAM -> startActivity(Intent(context, SetupEduroamActivity::class.java))
            BUTTON_LOGOUT -> showLogoutDialog(R.string.logout_title, R.string.logout_message)
            else -> return false
        }
        return true
    }

    private fun showLogoutDialog(title: Int, message: Int) {
        AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.logout) { _, _ -> logout() }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .apply {
                    window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
                }
                .show()
    }

    private fun logout() {
        try {
            clearData()
        } catch (e: Exception) {
            Utils.log(e)
            showLogoutDialog(R.string.logout_error_title, R.string.logout_try_again)
            return
        }

        startActivity(Intent(requireContext(), StartupActivity::class.java))
        requireActivity().finish()
    }

    @SuppressLint("ApplySharedPref")
    @Throws(ExecutionException::class, InterruptedException::class)
    private fun clearData() {
        TcaDb.resetDb(requireContext())
        requireContext().defaultSharedPreferences.edit().clear().commit()

        // Remove all notifications that are currently shown
        requireContext().notificationManager.cancelAll()

        val readCalendar = checkSelfPermission(requireActivity(), READ_CALENDAR)
        val writeCalendar = checkSelfPermission(requireActivity(), WRITE_CALENDAR)

        // Delete local calendar
        Utils.setSetting(requireContext(), Const.SYNC_CALENDAR, false)
        if (readCalendar == PERMISSION_GRANTED && writeCalendar == PERMISSION_GRANTED) {
            CalendarController.deleteLocalCalendar(requireContext())
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    companion object {

        private const val BUTTON_LOGOUT = "button_logout"
        private const val SETUP_EDUROAM = "card_eduroam_setup"

        fun newInstance(
            key: String?
        ) = SettingsFragment().apply { arguments = bundleOf(ARG_PREFERENCE_ROOT to key) }
    }
}
