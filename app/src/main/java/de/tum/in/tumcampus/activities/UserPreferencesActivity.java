package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.fragments.SettingsFragment;

/**
 * Provides the preferences, encapsulated into an own activity.
 */
@SuppressWarnings("deprecation")
public class UserPreferencesActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        SettingsFragment f = new SettingsFragment();

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey(Const.PREFERENCE_SCREEN)) {
            final String key = intent.getExtras().getString(Const.PREFERENCE_SCREEN);

            Bundle args = new Bundle();
            args.putString(Const.PREFERENCE_SCREEN, key);
            f.setArguments(args);
        }

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f).commit();
    }
}