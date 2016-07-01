package de.tum.in.tumcampusapp.activities.wizard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.auxiliary.AccessTokenManager;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.SurveyManager;

/**
 * Displays the first page of the startup wizard, where the user can enter his lrz-id.
 */
public class WizNavStartActivity extends ActivityForLoadingInBackground<Void, Boolean> implements OnClickListener {
    private final AccessTokenManager accessTokenManager = new AccessTokenManager(this);
    private EditText editTxtLrzId;
    private Spinner userMajorSpinner;
    private String lrzId;
    private SharedPreferences sharedPrefs;
    String userMajor = "";

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

        userMajorSpinner = (Spinner) findViewById(R.id.majorSpinner);
        setUpSpinner();

        editTxtLrzId = (EditText) findViewById(R.id.lrd_id);
        lrzId = sharedPrefs.getString(Const.LRZ_ID, "");
        if (lrzId != null) {
            editTxtLrzId.setText(lrzId);
        }
    }

    public void setUpSpinner() {
        // fetch facultyData from API
        new AsyncTask<Void, Void, String[]>() {
            @Override
            protected String[] doInBackground(Void... voids) {
                ArrayList<String> fetchedFaculties = new ArrayList<>();
                SurveyManager sm = new SurveyManager(getApplicationContext());
                try {
                    sm.downloadFacultiesFromExternal();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Cursor cursor = sm.getAllFaculties();
                if (cursor.moveToFirst()) {
                    do {
                        fetchedFaculties.add(cursor.getString(cursor.getColumnIndex("name")));
                    } while (cursor.moveToNext());

                }
                fetchedFaculties.add(0, getResources().getString(R.string.choose_own_faculty));
                final String[] majors = fetchedFaculties.toArray(new String[fetchedFaculties.size()]);

                return majors;
            }

            @Override
            protected void onPostExecute(String[] majors) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, majors);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userMajorSpinner.setAdapter(adapter);
                userMajorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        SurveyManager sm = new SurveyManager(getApplicationContext());
                        Cursor c = sm.getFacultyID((String) adapterView.getItemAtPosition(i));
                        if (c.moveToFirst()) {
                            Utils.setInternalSetting(getApplicationContext(), "user_major", c.getString(c.getColumnIndex("faculty")));
                        }
                        TextView selectedItem = (TextView) adapterView.getChildAt(0);
                        if (selectedItem != null) {
                            selectedItem.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_primary));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }

        }.execute();
    }

    /**
     * Handle click on skip button
     *
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
     *
     * @param next Next button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickNext(View next) {

        if (userMajorSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(getApplicationContext(), "Please select your faculty", Toast.LENGTH_SHORT).show();
            return;
        }

        lrzId = editTxtLrzId.getText().toString();
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
     *
     * @param dialog Dialog handle
     * @param which  Button clicked
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
     *
     * @param arg Unused
     * @return True if the access token was successfully created
     */
    @Override
    protected Boolean onLoadInBackground(Void... arg) {
        return accessTokenManager.requestAccessToken(WizNavStartActivity.this, lrzId);
    }

    /**
     * Opens second wizard page if access token available
     *
     * @param result Was access token successfully created
     */
    @Override
    protected void onLoadFinished(Boolean result) {
        if (result) {
            finish();
            startActivity(new Intent(this, WizNavCheckTokenActivity.class));
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        } else {
            showLoadingEnded();
        }
    }
}
