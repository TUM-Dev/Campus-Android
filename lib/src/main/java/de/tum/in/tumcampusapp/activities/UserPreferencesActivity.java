package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.os.Bundle;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.ImplicitCounter;
import de.tum.in.tumcampusapp.fragments.SettingsFragment;

/**
 * Provides the preferences, encapsulated into an own activity.
 */
public class UserPreferencesActivity extends BaseActivity {

    public UserPreferencesActivity() {
        super(R.layout.activity_user_preferences);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);

        SettingsFragment f = new SettingsFragment();

        //Enable the direct access of a specific sub section, e.g.: cards
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