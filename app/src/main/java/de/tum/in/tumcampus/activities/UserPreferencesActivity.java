package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.fragments.SettingsFragment;

/**
 * Provides the preferences, encapsulated into an own activity.
 */
public class UserPreferencesActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_preferences);
        ImplicitCounter.Counter(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        SettingsFragment f = new SettingsFragment();

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey(Const.PREFERENCE_SCREEN)) {
            final String key = intent.getExtras().getString(Const.PREFERENCE_SCREEN);

            Bundle args = new Bundle();
            args.putString(Const.PREFERENCE_SCREEN, key);
            f.setArguments(args);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.settings_frame, f).commit();
    }
}