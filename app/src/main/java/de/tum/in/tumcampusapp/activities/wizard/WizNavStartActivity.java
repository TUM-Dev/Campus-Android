package de.tum.in.tumcampusapp.activities.wizard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.auxiliary.AccessTokenManager;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Displays the first page of the startup wizard, where the user can enter his lrz-id.
 */
public class WizNavStartActivity extends ActivityForLoadingInBackground<Void,Boolean> implements OnClickListener {
	private final AccessTokenManager accessTokenManager = new AccessTokenManager(this);
	private EditText editText;
    private Spinner userMajorSpinner;
	private String lrzId;
	private SharedPreferences sharedPrefs;
    String userMajor="";

    public WizNavStartActivity() {
        super(R.layout.activity_wiznav_start);
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        disableRefresh();

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		LinearLayout layout = (LinearLayout) findViewById(R.id.wizard_start_layout);
		layout.requestFocus();

        userMajorSpinner=(Spinner)  findViewById(R.id.majorSpinner);
		editText = (EditText) findViewById(R.id.lrd_id);
        setUpSpinner();
		lrzId = sharedPrefs.getString(Const.LRZ_ID, "");
		if (lrzId != null) {
			editText.setText(lrzId);
		}
	}

    public void setUpSpinner()
    {
        String select = "Please Select Your Major";
        String math = getResources().getString(R.string.faculty_mathematics);
        String physics = getResources().getString(R.string.faculty_physics);
        String chemistry = getResources().getString(R.string.faculty_chemistry);
        String tum_manag = getResources().getString(R.string.faculty_tum_school_of_management);
        String cge = getResources().getString(R.string.faculty_civil_geo_and_environmental_engineering);
        String architecture = getResources().getString(R.string.faculty_architecture);
        String mechanical = getResources().getString(R.string.faculty_mechanical_Engineering);
        String electrical = getResources().getString(R.string.faculty_electrical_and_computer_engineering);
        String informatics = getResources().getString(R.string.faculty_informatics);
        String tum_life_sc = getResources().getString(R.string.faculty_tum_school_of_life_sciences_weihenstephan);
        String medicine = getResources().getString(R.string.faculty_tum_school_of_medicine);
        String sport = getResources().getString(R.string.faculty_sport_and_health_sciences);
        String edu = getResources().getString(R.string.faculty_tum_school_of_education);
        String political_social = getResources().getString(R.string.faculty_political_and_social_sciences);
        String[] majors = {select,math, physics, chemistry, tum_manag, cge, architecture, mechanical, electrical, informatics, tum_life_sc, medicine, sport, edu, political_social};

       ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                android.R.layout.simple_list_item_1, majors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userMajorSpinner.setAdapter(adapter);

        userMajorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Handle click on skip button
     * @param skip Skip button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickSkip(View skip) {
        finish();
        if (new AccessTokenManager(this).hasValidAccessToken()) {
            startActivity(new Intent(this, WizNavChatActivity.class));
        } else {
            startActivity(new Intent(this, WizNavExtrasActivity.class));
        }
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * Handle click on next button
     * @param next Next button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickNext(View next) {

        if(userMajorSpinner.getSelectedItemPosition()==0)
        {
            Toast.makeText(getApplicationContext(),"Please select your major",Toast.LENGTH_SHORT).show();
            return;
        }

        userMajor=userMajorSpinner.getSelectedItem().toString();
        lrzId = editText.getText().toString();
        Editor editor = sharedPrefs.edit();
        editor.putString(Const.LRZ_ID, lrzId);
        editor.apply();

		// check if lrz could be valid?
		if (lrzId.length() >= AccessTokenManager.MIN_LRZ_LENGTH) {
			// is access token already set?
			if (accessTokenManager.hasValidAccessToken()) {
				// show Dialog first
				new AlertDialog.Builder(this)
				        .setMessage(getString(R.string.dialog_new_token))
						.setPositiveButton(getString(R.string.yes), this)
						.setNegativeButton(getString(R.string.no), this)
						.show();
			} else {
                startLoading();
			}
		} else {
			Utils.showToast(this, R.string.error_lrz_wrong);
		}
	}

    /**
     * Handle click in dialog buttons
     * @param dialog Dialog handle
     * @param which Button clicked
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            startLoading();

        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            onLoadFinished(true);
        }
    }

    /**
     * Requests an access-token from the TumOnline server in background
     * @param arg Unused
     * @return True if the access token was successfully created
     */
    @Override
    protected Boolean onLoadInBackground(Void... arg) {
        return accessTokenManager.requestAccessToken(WizNavStartActivity.this, lrzId);
    }

    /**
     * Opens second wizard page if access token available
     * @param result Was access token successfully created
     */
    @Override
    protected void onLoadFinished(Boolean result) {
        if(result) {
            finish();
            startActivity(new Intent(this, WizNavCheckTokenActivity.class));
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        } else {
            showLoadingEnded();
        }
    }
}
