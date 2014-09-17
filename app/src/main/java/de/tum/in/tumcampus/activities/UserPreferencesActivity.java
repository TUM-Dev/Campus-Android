package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.Toast;

import de.psdev.licensesdialog.LicensesDialog;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.wizzard.WizNavStartActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CafeteriaManager;
import de.tum.in.tumcampus.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.managers.CalendarManager;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.NewsManager;
import de.tum.in.tumcampus.models.managers.SyncManager;
import de.tum.in.tumcampus.services.BackgroundService;
import de.tum.in.tumcampus.services.SilenceService;

/**
 * Provides the preferences, capsulated into an own activity.
 *
 * @author Sascha Moecker
 */
public class UserPreferencesActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener, OnClickListener {

    private AccessTokenManager accessTokenManager = new AccessTokenManager(this);
    private Context context = this;

    /**
     * Clears all downlaoded data from SD card and database
     *
     * @return true, if successful
     */
    private boolean clearCache() { //TODO remove this option/clean up too old cache content on startup
        try {
            Utils.getCacheDir("");
        } catch (Exception e) {
            Toast.makeText(context, R.string.exception_sdcard,
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        CafeteriaManager cm = new CafeteriaManager(context);
        cm.removeCache();

        CafeteriaMenuManager cmm = new CafeteriaMenuManager(context);
        cmm.removeCache();

        NewsManager nm = new NewsManager(context);
        nm.removeCache();

        CalendarManager calendarManager = new CalendarManager(context);
        calendarManager.removeCache();

        // table of all download events
        SyncManager sm = new SyncManager(context);
        sm.deleteFromDb();

        // delete local calendar
        if (Build.VERSION.SDK_INT >= 14) {
            CalendarManager.deleteLocalCalendar(this);
        }

        Toast.makeText(context, R.string.success_clear_cache, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            clearCache();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        // Click listener for preference list entries. Used to simulate a button
        // (since it is not possible to add a button to the preferences screen)
        Preference buttonToken = findPreference("button_token");
        buttonToken
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        // Querys for a access token from TUMOnline
                        accessTokenManager.setupAccessToken();
                        return true;
                    }
                });
        // This button invokes the wizzard to open. It pretents to be a "button"
        // though the preference do not provide buttons
        Preference buttonWizzard = findPreference("button_wizzard");
        buttonWizzard
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        finish();
                        Intent intent = new Intent(
                                UserPreferencesActivity.this,
                                WizNavStartActivity.class);
                        startActivity(intent);
                        return true;
                    }
                });
        // This button invokes the clear cache method
        Preference buttonClearCache = findPreference("button_clear_cache");
        buttonClearCache
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context);
                        builder.setMessage(
                                context.getString(R.string.delete_chache_sure))
                                .setPositiveButton(
                                        context.getString(R.string.yes),
                                        UserPreferencesActivity.this)
                                .setNegativeButton(
                                        context.getString(R.string.no),
                                        UserPreferencesActivity.this).show();

                        return true;
                    }
                });

        CheckBoxPreference silent = (CheckBoxPreference) findPreference("silent_mode");
        if(!new AccessTokenManager(this).hasValidAccessToken()) {
            silent.setEnabled(false);
        }

        // Open the facebook app or view in a browser when not installed
        Preference facebookPref = findPreference("facebook");
        facebookPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent facebook;
                try {
                    //Try to get facebook package to check if fb app is installed
                    context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
                    facebook = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_link_app)));
                } catch (Exception e) {
                    //otherwise just open the normal url
                    facebook = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.facebook_link)));
                }
                startActivity(facebook);
                return true;
            }
        });

        // Open the facebook app or view in a browser when not installed
        Preference githubPref = findPreference("github");
        githubPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_link))));
                return true;
            }
        });

        // Show first use tutorial
        Preference firstUsePref = findPreference("first_run");
        firstUsePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(UserPreferencesActivity.this);
                SharedPreferences.Editor e = sharedPreferences.edit();
                e.putBoolean(CardManager.SHOW_TUTORIAL_1, true);
                e.putBoolean(CardManager.SHOW_TUTORIAL_2, true);
                e.apply();
                CardManager.update(UserPreferencesActivity.this);
                startActivity(new Intent(UserPreferencesActivity.this, StartActivity.class));
                return true;
            }
        });


        // Show licences
        Preference licencesPref = findPreference("licenses");
        licencesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                new LicensesDialog(UserPreferencesActivity.this, R.raw.notices, false, true).show();
                return true;
            }
        });

        // Show licences
        Preference feedbackPref = findPreference("feedback");
        feedbackPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                /* Create the Intent */
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL,getString(R.string.feedbackAddr));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.feedbackSubj));

		        /* Send it off to the Activity-Chooser */
                startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
                return true;
            }
        });

        setSummary("card_cafeteria_default_G");
        setSummary("card_cafeteria_default_K");
        setSummary("card_cafeteria_default_W");
        setSummary("card_role");
        setSummary("card_stations_default_G");
        setSummary("card_stations_default_C");
        setSummary("card_stations_default_K");

        // Register the change listener to react immediately on changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        Intent intent = getIntent();
        // Open a card's preference screen if selected from it's context menu
        if(intent!=null && intent.getExtras()!=null && intent.getExtras().containsKey(Const.PREFERENCE_SCREEN)) {
            final String key = intent.getExtras().getString(Const.PREFERENCE_SCREEN);
            PreferenceScreen screen = (PreferenceScreen) findPreference("cards_pref_container");
            Preference cardPreferenceScreen = findPreference(key);
            final ListAdapter listAdapter = screen.getRootAdapter();
            final int itemsCount = listAdapter.getCount();
            for (int i = 0; i < itemsCount; ++i) {
                if (listAdapter.getItem(i).equals(cardPreferenceScreen)) {
                    screen.onItemClick(null, null, i, 0);
                    break;
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if(pref instanceof ListPreference) {
            ListPreference listPreference = (ListPreference)pref;
            listPreference.setSummary(listPreference.getEntry());
        }

        if(key.startsWith("card_")) {
            CardManager.shouldRefresh = true;
        }

        // If the silent mode was activated, start the service. This will invoke
        // the service to call onHandleIntent which checks available lectures
        if (key.equals(Const.SILENCE_SERVICE)) {
            Intent service = new Intent(this, SilenceService.class);
            if (sharedPreferences.getBoolean(key, false)) {
                startService(service);
            } else {
                stopService(service);
            }
        }

        // If the background mode was activated, start the service. This will invoke
        // the service to call onHandleIntent which updates all background data
        if (key.equals(Const.BACKGROUND_MODE)) {
            Intent service = new Intent(this, BackgroundService.class);
            if (sharedPreferences.getBoolean(key, false)) {
                startService(service);
            } else {
                stopService(service);
            }
        }
    }

    public void setSummary(String key) {
        Preference t = findPreference(key);
        if(t instanceof ListPreference) {
            ListPreference pref = (ListPreference)t;
            pref.setSummary(pref.getEntry());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}