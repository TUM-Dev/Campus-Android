package de.tum.in.tumcampusapp.component.other.settings;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import android.view.View;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.AccessTokenManager;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarController;
import de.tum.in.tumcampusapp.component.ui.eduroam.SetupEduroamActivity;
import de.tum.in.tumcampusapp.component.ui.news.NewsController;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.component.ui.onboarding.StartupActivity;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.service.BackgroundService;
import de.tum.in.tumcampusapp.service.SilenceService;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    public static final String FRAGMENT_TAG = "my_preference_fragment";
    private static final String BUTTON_LOGOUT = "button_logout";
    private static final String SETUP_EDUROAM = "card_eduroam_setup";

    private FragmentActivity mContext;

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        // Load the correct preference category
        setPreferencesFromResource(R.xml.settings, rootKey);
        mContext = getActivity();

        populateNewsSources();
        setUpEmployeeSettings();

        // Disables silence service if the app is used without TUMOnline access
        SwitchPreferenceCompat silentSwitch =
                (SwitchPreferenceCompat) findPreference(Const.SILENCE_SERVICE);
        if (silentSwitch != null && !AccessTokenManager.hasValidAccessToken(getContext())) {
            silentSwitch.setEnabled(false);
        }

        // Only do these things if we are in the root of the preferences
        if (rootKey == null) {
            // Click listener for preference list entries. Used to simulate a button
            // (since it is not possible to add a button to the preferences screen)
            findPreference(BUTTON_LOGOUT).setOnPreferenceClickListener(this);

            setSummary("card_default_campus");
            setSummary("silent_mode_set_to");
            setSummary("background_mode_set_to");
        } else if (rootKey.equals("card_cafeteria")) {
            setSummary("card_cafeteria_default_G");
            setSummary("card_cafeteria_default_K");
            setSummary("card_cafeteria_default_W");
            setSummary("card_role");
        } else if (rootKey.equals("card_mvv")) {
            setSummary("card_stations_default_G");
            setSummary("card_stations_default_C");
            setSummary("card_stations_default_K");
        } else if (rootKey.equals("card_eduroam")) {
            findPreference(SETUP_EDUROAM).setOnPreferenceClickListener(this);
        }

        // Register the change listener to react immediately on changes
        PreferenceManager.getDefaultSharedPreferences(mContext)
                         .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the default white background in the view so as to avoid transparency
        view.setBackgroundColor(Color.WHITE);
    }

    private void populateNewsSources() {
        PreferenceCategory newsSourcesPreference =
                (PreferenceCategory) findPreference("card_news_sources");

        NewsController newsController = new NewsController(mContext);
        List<NewsSources> newsSources = newsController.getNewsSources();
        for (NewsSources newsSource : newsSources) {
            final CheckBoxPreference pref = new CheckBoxPreference(mContext);
            pref.setKey("card_news_source_" + newsSource.getId());
            pref.setDefaultValue(true);
            
            // reserve space so that when the icon is loaded the text is not moved again
            pref.setIconSpaceReserved(true);

            // Load news source icon in background and set it
            final String url = newsSource.getIcon();
            if (!url.trim().isEmpty()) { // Skip News that do not have a image
                new Thread(() -> {
                    try {
                        Bitmap bmp = Picasso.get().load(url).get();
                        mContext.runOnUiThread(() -> {
                            if(isAdded()){
                                pref.setIcon(new BitmapDrawable(getResources(), bmp));
                            }
                        });
                    } catch (IOException e) {
                        // ignore
                    }
                }).start();
            }

            pref.setTitle(newsSource.getTitle());
            if (newsSourcesPreference != null) {
                newsSourcesPreference.addPreference(pref);
            }
        }
    }

    /**
     * Disable setting for non-employees.
     */
    private void setUpEmployeeSettings() {
        boolean isEmployee = !Utils.getSetting(mContext, Const.TUMO_EMPLOYEE_ID, "").isEmpty();
        if (!isEmployee) {
            findPreference(Const.EMPLOYEE_MODE).setEnabled(false);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) pref;
            String entry = listPreference.getEntry().toString();
            listPreference.setSummary(entry);
        }

        // When newspread selection changes
        // deselect all newspread sources and select only the
        // selected source if one of all was selected before
        if ("news_newspread".equals(key)) {
            SharedPreferences.Editor e = sharedPreferences.edit();
            boolean value = false;
            for (int i = 7; i < 14; i++) {
                if (sharedPreferences.getBoolean("card_news_source_" + i, false)) {
                    value = true;
                }
                e.putBoolean("card_news_source_" + i, false);
            }
            String newSource = sharedPreferences.getString(key, "7");
            e.putBoolean("card_news_source_" + newSource, value);
            e.apply();
        }

        // If the silent mode was activated, start the service. This will invoke
        // the service to call onHandleIntent which checks available lectures
        if (key.equals(Const.SILENCE_SERVICE)) {
            Intent service = new Intent(mContext, SilenceService.class);
            if (sharedPreferences.getBoolean(key, false)) {
                if (!SilenceService.hasPermissions(mContext)) {
                    // disable until silence service permission is resolved
                    SwitchPreferenceCompat silenceSwitch =
                            (SwitchPreferenceCompat) findPreference(Const.SILENCE_SERVICE);
                    if (silenceSwitch != null) {
                        silenceSwitch.setChecked(false);
                    }
                    Utils.setSetting(mContext, Const.SILENCE_SERVICE, false);

                    SilenceService.requestPermissions(mContext);
                } else {
                    mContext.startService(service);
                }
            } else {
                mContext.stopService(service);
            }
        }

        // If the background mode was activated, start the service. This will invoke
        // the service to call onHandleIntent which updates all background data
        if (key.equals(Const.BACKGROUND_MODE)) {
            Intent service = new Intent(mContext, BackgroundService.class);
            if (sharedPreferences.getBoolean(key, false)) {
                mContext.startService(service);
            } else {
                mContext.stopService(service);
            }
        }
    }

    private void setSummary(CharSequence key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            String entry = listPref.getEntry().toString();
            listPref.setSummary(entry);
        }
    }

    /**
     * Handle all clicks on 'button'-preferences
     *
     * @param preference Preference that has been clicked
     * @return True, if handled
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String key = preference.getKey();

        switch (key) {
            case SETUP_EDUROAM:
                startActivity(new Intent(getContext(), SetupEduroamActivity.class));
                break;
            case BUTTON_LOGOUT:
                showLogoutDialog();
                break;
            default:
                return false;
        }

        return true;
    }

    private void showLogoutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.logout, ((dialogInterface, i) -> logout()))
                .setNegativeButton(R.string.cancel, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

    private void logout() {
        clearData();
        startActivity(new Intent(mContext, StartupActivity.class));
        mContext.finish();
    }

    private void clearData() {
        TcaDb.resetDb(mContext);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPrefs.edit().clear().apply();

        // Remove all notifications that are currently shown
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        int readCalendar = ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.READ_CALENDAR);
        int writeCalendar = ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.WRITE_CALENDAR);

        // Delete local calendar
        Utils.setSetting(mContext, Const.SYNC_CALENDAR, false);
        if (readCalendar == PackageManager.PERMISSION_GRANTED &&
                writeCalendar == PackageManager.PERMISSION_GRANTED) {
            CalendarController.deleteLocalCalendar(mContext);
        }
    }

}