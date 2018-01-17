package de.tum.in.tumcampusapp.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import android.view.View;

import com.google.common.base.Optional;

import java.util.List;

import de.psdev.licensesdialog.LicensesDialog;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.SetupEduroamActivity;
import de.tum.in.tumcampusapp.activities.StartupActivity;
import de.tum.in.tumcampusapp.activities.wizard.WizNavStartActivity;
import de.tum.in.tumcampusapp.auxiliary.AccessTokenManager;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.AbstractManager;
import de.tum.in.tumcampusapp.managers.CalendarManager;
import de.tum.in.tumcampusapp.managers.CardManager;
import de.tum.in.tumcampusapp.managers.NewsManager;
import de.tum.in.tumcampusapp.models.tumcabe.NewsSources;
import de.tum.in.tumcampusapp.services.BackgroundService;
import de.tum.in.tumcampusapp.services.SilenceService;

public class SettingsFragment extends PreferenceFragmentCompat implements
                                                               SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    public static final String FRAGMENT_TAG = "my_preference_fragment";
    private static final String BUTTON_WIZARD = "button_wizard";
    private static final String BUTTON_CLEAR_CACHE = "button_clear_cache";
    private static final String FACEBOOK = "facebook";
    private static final String GITHUB = "github";
    private static final String LICENSES = "licenses";
    private static final String FEEDBACK = "feedback";
    private static final String PRIVACY = "privacy";
    private static final String SETUP_EDUROAM = "card_eduroam_setup";
    private FragmentActivity mContext;

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        //Load the correct preference category
        setPreferencesFromResource(R.xml.settings, rootKey);
        mContext = getActivity();

        // Disables silence service if the app is used without TUMOnline access
        CheckBoxPreference silent = (CheckBoxPreference) findPreference("silent_mode");
        if (silent != null && !new AccessTokenManager(mContext).hasValidAccessToken()) {
            silent.setEnabled(false);
        }

        //Only do these things if we are in the root of the preferences
        if (rootKey == null) {
            // Click listener for preference list entries. Used to simulate a button
            // (since it is not possible to add a button to the preferences screen)
            findPreference(BUTTON_WIZARD).setOnPreferenceClickListener(this);
            findPreference(BUTTON_CLEAR_CACHE).setOnPreferenceClickListener(this);
            findPreference(FACEBOOK).setOnPreferenceClickListener(this);
            findPreference(GITHUB).setOnPreferenceClickListener(this);
            findPreference(LICENSES).setOnPreferenceClickListener(this);
            findPreference(FEEDBACK).setOnPreferenceClickListener(this);
            findPreference(PRIVACY).setOnPreferenceClickListener(this);

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
        PreferenceCategory newsSourcesPreference = (PreferenceCategory) findPreference("card_news_sources");

        NewsManager newsManager = new NewsManager(mContext);
        List<NewsSources> newsSources = newsManager.getNewsSources();
        final NetUtils net = new NetUtils(mContext);
        for (NewsSources newsSource: newsSources) {
            final CheckBoxPreference pref = new CheckBoxPreference(mContext);
            pref.setKey("card_news_source_" + newsSource.getId());
            pref.setDefaultValue(true);
            // Load news source icon in background and set it
            final String url = newsSource.getIcon();

            if (url != null) { // Skip News that do not have a image
                new Thread(() -> {
                    final Optional<Bitmap> bmp = net.downloadImageToBitmap(url);
                    mContext.runOnUiThread(() -> {
                        if (bmp.isPresent()) {
                            pref.setIcon(new BitmapDrawable(getResources(), bmp.get()));
                        }
                    });
                }).start();
            }

            pref.setTitle(newsSource.getTitle());
            newsSourcesPreference.addPreference(pref);
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
                    CheckBoxPreference silenceCheckbox = (CheckBoxPreference) findPreference(Const.SILENCE_SERVICE);
                    silenceCheckbox.setChecked(false);
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
            case BUTTON_WIZARD:
                mContext.finish();
                startActivity(new Intent(mContext, WizNavStartActivity.class));
                break;
            case BUTTON_CLEAR_CACHE:
                // This button invokes the clear cache method
                new AlertDialog.Builder(mContext)
                        .setMessage(R.string.delete_chache_sure)
                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> clearCache())
                        .setNegativeButton(R.string.no, null)
                        .show();

                break;
            case FACEBOOK:
                // Open the facebook app or view in a browser when not installed
                Intent facebook;
                try {
                    //Try to get facebook package to check if fb app is installed
                    mContext.getPackageManager()
                            .getPackageInfo("com.facebook.katana", 0);
                    facebook = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_link_app)));
                } catch (PackageManager.NameNotFoundException e) {
                    //otherwise just open the normal url
                    facebook = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_link)));
                }
                startActivity(facebook);
                break;
            case GITHUB:
                // Open TCA-github web page
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_link))));
                break;
            case LICENSES:
                // Show licences
                new LicensesDialog.Builder(mContext)
                        .setNotices(R.raw.notices)
                        .setShowFullLicenseText(false)
                        .setIncludeOwnLicense(true)
                        .build()
                        .show();
                break;
            case FEEDBACK:
            /* Create the Intent */
                Uri uri = Uri.parse("mailto:tca-support.os.in@tum.de?subject=" + getString(R.string.feedbackSubj));

                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                sendIntent.setData(uri);

		    /* Send it off to the Activity-Chooser */
                startActivity(Intent.createChooser(sendIntent, getString(R.string.send_email)));
                break;
            case PRIVACY:
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_privacy_policy)));
                startActivity(myIntent);
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
        AbstractManager.resetDb(mContext);

        // delete local calendar
        Utils.setInternalSetting(mContext, Const.SYNC_CALENDAR, false);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            CalendarManager.deleteLocalCalendar(mContext);
        }

        Utils.showToast(mContext, R.string.success_clear_cache);
        Utils.setInternalSetting(mContext, Const.EVERYTHING_SETUP, false);

        mContext.finish();
        startActivity(new Intent(mContext, StartupActivity.class));
    }
}