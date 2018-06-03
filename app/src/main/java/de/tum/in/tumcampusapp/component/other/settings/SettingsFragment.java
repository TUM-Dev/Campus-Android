package de.tum.in.tumcampusapp.component.other.settings;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
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
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.service.BackgroundService;
import de.tum.in.tumcampusapp.service.SilenceService;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    public static final String FRAGMENT_TAG = "my_preference_fragment";
    private static final String BUTTON_CLEAR_CACHE = "button_clear_cache";
    private static final String SETUP_EDUROAM = "card_eduroam_setup";
    private FragmentActivity mContext;

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        //Load the correct preference category
        setPreferencesFromResource(R.xml.settings, rootKey);
        mContext = getActivity();

        // Disables silence service if the app is used without TUMOnline access
        SwitchPreferenceCompat silentSwitch =
                (SwitchPreferenceCompat) findPreference(Const.SILENCE_SERVICE);
        if (silentSwitch != null && !new AccessTokenManager(mContext).hasValidAccessToken()) {
            silentSwitch.setEnabled(false);
        }

        //Only do these things if we are in the root of the preferences
        if (rootKey == null) {
            // Click listener for preference list entries. Used to simulate a button
            // (since it is not possible to add a button to the preferences screen)
            findPreference(BUTTON_CLEAR_CACHE).setOnPreferenceClickListener(this);

            // Set summary for these preferences
            setSummary("card_cafeteria_default_G");
            setSummary("card_cafeteria_default_K");
            setSummary("card_cafeteria_default_W");
            setSummary("card_role");
            setSummary("card_stations_default_G");
            setSummary("card_stations_default_C");
            setSummary("card_stations_default_K");
            setSummary("card_default_campus");
            setSummary("silent_mode_set_to");
            setSummary("background_mode_set_to");

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

        // Populate news sources
        populateNewsSources();
    }

    private void populateNewsSources() {
        PreferenceCategory newsSourcesPreference =
                (PreferenceCategory) findPreference("card_news_sources");

        NewsController newsController = new NewsController(mContext);
        List<NewsSources> newsSources = newsController.getNewsSources();
        final NetUtils net = new NetUtils(mContext);
        for (NewsSources newsSource : newsSources) {
            final CheckBoxPreference pref = new CheckBoxPreference(mContext);
            pref.setKey("card_news_source_" + newsSource.getId());
            pref.setDefaultValue(true);

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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) pref;
            listPreference.setSummary(listPreference.getEntry());
        }

        //Refresh the cards after a change has been made to them
        if (key.startsWith("card_")) {
            CardManager.setShouldRefresh();
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
            CardManager.setShouldRefresh();
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
        Preference t = findPreference(key);
        if (t instanceof ListPreference) {
            ListPreference pref = (ListPreference) t;
            pref.setSummary(pref.getEntry());
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
            case BUTTON_CLEAR_CACHE:
                // This button invokes the clear cache method
                new AlertDialog.Builder(mContext)
                        .setMessage(R.string.delete_chache_sure)
                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> clearCache())
                        .setNegativeButton(R.string.no, null)
                        .show();
                break;
            default:
                return false;
        }

        return true;
    }

    /**
     * Clears all downloaded data from SD card and database
     */
    private void clearCache() {
        TcaDb.resetDb(mContext);

        // delete local calendar
        Utils.setSetting(mContext, Const.SYNC_CALENDAR, false);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            CalendarController.deleteLocalCalendar(mContext);
        }

        Utils.showToast(mContext, R.string.success_clear_cache);
        Utils.setSetting(mContext, Const.EVERYTHING_SETUP, false);

        mContext.finish();
        startActivity(new Intent(mContext, StartupActivity.class));
    }
}