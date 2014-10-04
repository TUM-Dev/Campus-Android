package de.tum.in.tumcampus.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.widget.ListAdapter;

import de.psdev.licensesdialog.LicensesDialog;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.wizard.WizNavStartActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
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
 * Provides the preferences, encapsulated into an own activity.
 */
@SuppressWarnings("deprecation")
public class UserPreferencesActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private final AccessTokenManager accessTokenManager = new AccessTokenManager(this);

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);

        //Load all the available settings
        addPreferencesFromResource(R.xml.settings);

        // @kb: ??? What is this supposed to do
        CheckBoxPreference silent = (CheckBoxPreference) findPreference("silent_mode");
        if(!new AccessTokenManager(this).hasValidAccessToken()) {
            silent.setEnabled(false);
        }

        // Click listener for preference list entries. Used to simulate a button
        // (since it is not possible to add a button to the preferences screen)
        findPreference("button_token").setOnPreferenceClickListener(this);
        findPreference("button_wizard").setOnPreferenceClickListener(this);
        findPreference("button_clear_cache").setOnPreferenceClickListener(this);
        findPreference("facebook").setOnPreferenceClickListener(this);
        findPreference("github").setOnPreferenceClickListener(this);
        findPreference("first_run").setOnPreferenceClickListener(this);
        findPreference("licenses").setOnPreferenceClickListener(this);
        findPreference("feedback").setOnPreferenceClickListener(this);

        // Set summary for these preferences
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

        // Populate news sources
        populateNewsSources();

        // Open a card's preference screen if selected from it's context menu
        Intent intent = getIntent();
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

    private void populateNewsSources() {
        PreferenceCategory news_sources = (PreferenceCategory) findPreference("card_news_sources");
        NewsManager cm = new NewsManager(this);
        Cursor cur = cm.getNewsSources();
        if(cur.moveToFirst()) {
            do {
                final CheckBoxPreference pref = new CheckBoxPreference(this);
                pref.setKey("card_news_source_"+cur.getString(0));
                pref.setDefaultValue(false);
                if(Build.VERSION.SDK_INT>=11) {
                    // Load news source icon in background and set it
                    final String url = cur.getString(1);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap bmp = Utils.downloadImageToBitmap(UserPreferencesActivity.this, url);
                            runOnUiThread(new Runnable() {
                                @TargetApi(11)
                                @Override
                                public void run() {
                                    pref.setIcon(new BitmapDrawable(getResources(), bmp));
                                }
                            });
                        }
                    }).start();
                }
                pref.setTitle(cur.getString(2));
                news_sources.addPreference(pref);
            } while(cur.moveToNext());
        }
        cur.close();
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

    @SuppressWarnings("deprecation")
    void setSummary(String key) {
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

    /**
     * Handle all clicks on 'button'-preferences
     * @param preference Preference that has been clicked
     * @return True, if handled
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String key = preference.getKey();

        if(key.equals("button_token")) {
            // Querys for a access token from TUMOnline
            accessTokenManager.setupAccessToken(this);


        } else if(key.equals("button_wizard")) {
            finish();
            startActivity(new Intent(this, WizNavStartActivity.class));


        } else if(key.equals("button_clear_cache")) {
            // This button invokes the clear cache method
            new AlertDialog.Builder(this)
                    .setMessage(R.string.delete_chache_sure)
                    .setPositiveButton(R.string.yes, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            clearCache();
                        }
                    })
                    .setNegativeButton(R.string.no, null).show();


        } else if(key.equals("facebook")) {
            // Open the facebook app or view in a browser when not installed
            Intent facebook;
            try {
                //Try to get facebook package to check if fb app is installed
                getPackageManager().getPackageInfo("com.facebook.katana", 0);
                facebook = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_link_app)));
            } catch (Exception e) {
                //otherwise just open the normal url
                facebook = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_link)));
            }
            startActivity(facebook);


        } else if(key.equals("github")) {
            // Open TCA-github web page
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_link))));


        } else if(key.equals("first_run")) {
            // Show first use tutorial
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor e = prefs.edit();
            e.putBoolean(CardManager.SHOW_TUTORIAL_1, true);
            e.putBoolean(CardManager.SHOW_TUTORIAL_2, true);
            e.apply();
            CardManager.update(this);
            startActivity(new Intent(this, MainActivity.class));


        } else if(key.equals("licenses")) {
            // Show licences
            new LicensesDialog(this, R.raw.notices, false, true).show();


        } else if(key.equals("feedback")) {
            /* Create the Intent */
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.feedbackAddr));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedbackSubj));

		    /* Send it off to the Activity-Chooser */
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
        } else {
            return false;
        }
        return true;
    }

    /**
     * Clears all downloaded data from SD card and database
     */
    //TODO remove this option/clean up too old cache content on startup
    private void clearCache() {
        try {
            Utils.getCacheDir("");
        } catch (Exception e) {
            Utils.showToast(this, R.string.exception_sdcard);
            return;
        }

        CafeteriaManager cm = new CafeteriaManager(this);
        cm.removeCache();

        CafeteriaMenuManager cmm = new CafeteriaMenuManager(this);
        cmm.removeCache();

        NewsManager nm = new NewsManager(this);
        nm.removeCache();

        CalendarManager calendarManager = new CalendarManager(this);
        calendarManager.removeCache();

        // table of all download events
        SyncManager sm = new SyncManager(this);
        sm.deleteFromDb();

        // delete local calendar
        if (Build.VERSION.SDK_INT >= 14) {
            CalendarManager.deleteLocalCalendar(this);
        }

        Utils.showToast(this, R.string.success_clear_cache);
    }
}