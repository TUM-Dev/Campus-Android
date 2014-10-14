package de.tum.in.tumcampus.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import de.psdev.licensesdialog.LicensesDialog;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.wizard.WizNavStartActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CacheManager;
import de.tum.in.tumcampus.models.managers.CalendarManager;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.DatabaseManager;
import de.tum.in.tumcampus.models.managers.NewsManager;
import de.tum.in.tumcampus.services.BackgroundService;
import de.tum.in.tumcampus.services.SilenceService;

/**
 * Provides the preferences, encapsulated into an own activity.
 */
@SuppressWarnings("deprecation")
public class UserPreferencesActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private boolean returnHome = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);

        //Load all the available settings
        addPreferencesFromResource(R.xml.settings);

        // @kb: ??? What is this supposed to do
        CheckBoxPreference silent = (CheckBoxPreference) findPreference("silent_mode");
        if (!new AccessTokenManager(this).hasValidAccessToken()) {
            silent.setEnabled(false);
        }

        // Click listener for preference list entries. Used to simulate a button
        // (since it is not possible to add a button to the preferences screen)
        findPreference("button_wizard").setOnPreferenceClickListener(this);
        findPreference("button_clear_cache").setOnPreferenceClickListener(this);
        findPreference("facebook").setOnPreferenceClickListener(this);
        findPreference("github").setOnPreferenceClickListener(this);
        findPreference("first_run").setOnPreferenceClickListener(this);
        findPreference("licenses").setOnPreferenceClickListener(this);
        findPreference("feedback").setOnPreferenceClickListener(this);
        findPreference("privacy").setOnPreferenceClickListener(this);

        // Set summary for these preferences
        setSummary("card_cafeteria_default_G");
        setSummary("card_cafeteria_default_K");
        setSummary("card_cafeteria_default_W");
        setSummary("card_role");
        setSummary("card_stations_default_G");
        setSummary("card_stations_default_C");
        setSummary("card_stations_default_K");
        setSummary("silent_mode_set_to");

        // Register the change listener to react immediately on changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // Populate news sources
        populateNewsSources();

        // Open a card's preference screen if selected from it's context menu
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey(Const.PREFERENCE_SCREEN)) {
            final String key = intent.getExtras().getString(Const.PREFERENCE_SCREEN);
            this.returnHome = intent.getExtras().getBoolean("returnHome", false); // Check for flag to return directly home

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
        if (cur.moveToFirst()) {
            do {
                final CheckBoxPreference pref = new CheckBoxPreference(this);
                pref.setKey("card_news_source_" + cur.getString(0));
                pref.setDefaultValue(cur.getInt(0) == 2);
                if (Build.VERSION.SDK_INT >= 11) {
                    // Load news source icon in background and set it
                    final String url = cur.getString(1);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            NetUtils net = new NetUtils(UserPreferencesActivity.this);
                            final Bitmap bmp = net.downloadImageToBitmap(url);
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
            } while (cur.moveToNext());
        }
        cur.close();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) pref;
            listPreference.setSummary(listPreference.getEntry());
        }

        if (key.startsWith("card_")) {
            CardManager.shouldRefresh = true;
        }

        // When newspread selection changes
        // deselect all newspread sources and select only the
        // selected source if one of all was selected before
        if (key.equals("news_newspread")) {
            SharedPreferences.Editor e = sharedPreferences.edit();
            boolean value = false;
            for (int i = 7; i < 14; i++) {
                if (sharedPreferences.getBoolean("card_news_source_" + i, false))
                    value = true;
                e.putBoolean("card_news_source_" + i, false);
            }
            String new_source = sharedPreferences.getString(key, "7");
            e.putBoolean("card_news_source_" + new_source, value);
            e.apply();
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
        if (t instanceof ListPreference) {
            ListPreference pref = (ListPreference) t;
            pref.setSummary(pref.getEntry());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        if (key.equals("button_wizard")) {
            finish();
            startActivity(new Intent(this, WizNavStartActivity.class));


        } else if (key.equals("button_clear_cache")) {
            // This button invokes the clear cache method
            new AlertDialog.Builder(this)
                    .setMessage(R.string.delete_chache_sure)
                    .setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            clearCache();
                        }
                    })
                    .setNegativeButton(R.string.no, null).show();

        } else if (key.equals("facebook")) {
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


        } else if (key.equals("github")) {
            // Open TCA-github web page
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_link))));


        } else if (key.equals("first_run")) {
            // Show first use tutorial
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor e = prefs.edit();
            e.putBoolean(CardManager.SHOW_TUTORIAL_1, true);
            e.putBoolean(CardManager.SHOW_TUTORIAL_2, true);
            e.apply();
            CardManager.update(this);
            startActivity(new Intent(this, MainActivity.class));


        } else if (key.equals("licenses")) {
            // Show licences
            new LicensesDialog(this, R.raw.notices, false, true).show();


        } else if (key.equals("feedback")) {
            /* Create the Intent */
            Uri uri = Uri.parse("mailto:tca-support.os.in@tum.de?subject=" + getString(R.string.feedbackSubj));

            Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
            sendIntent.setData(uri);

		    /* Send it off to the Activity-Chooser */
            startActivity(Intent.createChooser(sendIntent, getString(R.string.send_email)));
        } else if (key.equals("privacy")) {
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_privacy_policy)));
            startActivity(myIntent);
        } else {
            return false;
        }

        return true;
    }

    /**
     * Listen for opening sub-screens (nested) settings
     *
     * @param preferenceScreen
     * @param preference
     * @return
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        // If the user has clicked on a preference screen, set up the action bar
        if (preference instanceof PreferenceScreen) {
            initializeActionBar((PreferenceScreen) preference);
        }

        return false;
    }

    /**
     * Sets up the action bar for an {@link PreferenceScreen}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void initializeActionBar(PreferenceScreen preferenceScreen) {
        final Dialog dialog = preferenceScreen.getDialog();

        //Check if dialog is open and if we are on a supported android version
        if (dialog != null && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Initialize the action bar
            dialog.getActionBar().setDisplayHomeAsUpEnabled(true);

            //Setup a dialog back button pressed listener
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (returnHome) {
                        finish();
                        startActivity(new Intent(UserPreferencesActivity.this, MainActivity.class));
                    }
                }
            });

            // Apply custom home button area click listener to close the PreferenceScreen because PreferenceScreens are dialogs which swallow events instead of passing to the activity
            // Related Issue: https://code.google.com/p/android/issues/detail?id=4611
            View homeBtn = dialog.findViewById(android.R.id.home);

            if (homeBtn != null) {
                View.OnClickListener dismissDialogClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Hide this
                        dialog.dismiss();

                        //Go back to home if we came from the start screen
                        if (returnHome) {
                            finish();
                            startActivity(new Intent(UserPreferencesActivity.this, MainActivity.class));
                        }
                    }
                };
                // Prepare yourselves for some hacky programming
                ViewParent homeBtnContainer = homeBtn.getParent();

                // The home button is an ImageView inside a FrameLayout
                if (homeBtnContainer instanceof FrameLayout) {
                    ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();

                    if (containerParent instanceof LinearLayout) {
                        // This view also contains the title text, set the whole view as clickable
                        containerParent.setOnClickListener(dismissDialogClickListener);
                    } else {
                        // Just set it on the home button
                        ((FrameLayout) homeBtnContainer).setOnClickListener(dismissDialogClickListener);
                    }
                } else {
                    // The 'If all else fails' default case
                    homeBtn.setOnClickListener(dismissDialogClickListener);
                }
            }
        }
    }

    /**
     * Clears all downloaded data from SD card and database
     */
    private void clearCache() {
        DatabaseManager.resetDb(this);

        CacheManager manager = new CacheManager(this);
        manager.clearCache();

        // delete local calendar
        Utils.setInternalSetting(this, Const.SYNC_CALENDAR, false);
        if (Build.VERSION.SDK_INT >= 14) {
            CalendarManager.deleteLocalCalendar(this);
        }

        Utils.showToast(this, R.string.success_clear_cache);
        Utils.setInternalSetting(this, Const.EVERYTHING_SETUP, false);

        finish();
        startActivity(new Intent(this, StartupActivity.class));
    }
}