package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Calendars;
import android.widget.Toast;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.wizzard.WizNavStartActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.MultiSelectListPreference;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CafeteriaManager;
import de.tum.in.tumcampus.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.managers.CalendarManager;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.EventManager;
import de.tum.in.tumcampus.models.managers.FeedItemManager;
import de.tum.in.tumcampus.models.managers.GalleryManager;
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
    private boolean clearCache() {
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

        EventManager em = new EventManager(context);
        em.removeCache();

        FeedItemManager fim = new FeedItemManager(context);
        fim.removeCache();

        GalleryManager gm = new GalleryManager(context);
        gm.removeCache();

        NewsManager nm = new NewsManager(context);
        nm.removeCache();

        CalendarManager calendarManager = new CalendarManager(context);
        calendarManager.removeCache();

        // table of all download events
        SyncManager sm = new SyncManager(context);
        sm.deleteFromDb();

        // delete local calendar
        ContentResolver crv = getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        crv.delete(uri, " account_name = '"
                + getString(R.string.calendar_account_name) + "'", null);

        Toast.makeText(context, R.string.success_clear_cache,
                Toast.LENGTH_SHORT).show();
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

        //PreferenceManager.setDefaultValues(this, R.xml.settings, false);


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
                e.commit();
                CardManager.update(UserPreferencesActivity.this);
                startActivity(new Intent(UserPreferencesActivity.this, StartActivity.class));
                return true;
            }
        });

        // Add cafeterias to preferences
        addCafeterias();

        // Set summarys for card settings
        setSummary((MultiSelectListPreference) findPreference("card_tuition_fee_setting"));
        setSummary((MultiSelectListPreference) findPreference("card_mvv_setting"));
        setSummary((MultiSelectListPreference) findPreference("card_next_lecture_setting"));
        setSummary((MultiSelectListPreference) findPreference("card_cafeteria_setting"));

        // Register the change listener to react immediately on changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    // Uses CafeteriaManager to populate MyMensa Category
    private void addCafeterias() {
        PreferenceCategory myMensa = (PreferenceCategory) findPreference("my_mensa");
        myMensa.removeAll();

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);

        CafeteriaManager manager = new CafeteriaManager(this);
        Cursor cursor = manager.getAllFromDb("% %");
        if (cursor.moveToFirst()) {
            do {
                CheckBoxPreference preference = new CheckBoxPreference(this);
                final String key = cursor.getString(2);
                preference.setTitle(cursor.getString(0));
                preference.setSummary(cursor.getString(1));
                preference.setKey("mensa_" + key);
                preference.setChecked(sharedPreferences.getBoolean("mensa_" + key, true));
                myMensa.addPreference(preference);
            } while (cursor.moveToNext());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if(pref instanceof MultiSelectListPreference) {
            setSummary((MultiSelectListPreference) pref);
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

    private void setSummary(MultiSelectListPreference pref) {
        CharSequence[] checked = pref.getCheckedValues();
        String sum="";
        for(CharSequence check : checked) {
            if(!sum.isEmpty())
                sum+=", ";
            if(check.equals("1")) {
                sum+=getString(R.string.startpage);
            } else if(check.equals("2")) {
                sum+=getString(R.string.phone);
            } else if(check.equals("3")) {
                sum+=getString(R.string.wear);
            }
        }
        pref.setSummary(sum);
    }
}