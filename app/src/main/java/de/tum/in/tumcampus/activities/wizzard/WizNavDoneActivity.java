package de.tum.in.tumcampus.activities.wizzard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.WizzardActivity;
import de.tum.in.tumcampus.auxiliary.Const;

public class WizNavDoneActivity extends WizzardActivity {

    public void onClickDone(View view) {
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_wiznav_done);


        // Turn off wizzard cause we just completed it
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(Const.HIDE_WIZZARD_ON_STARTUP, true);
        editor.commit();


        setIntentForNextActivity(null);
        setIntentForPreviousActivity(new Intent(this, WizNavExtrasActivity.class));
    }
}
