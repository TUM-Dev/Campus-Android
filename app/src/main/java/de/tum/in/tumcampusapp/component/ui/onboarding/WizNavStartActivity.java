package de.tum.in.tumcampusapp.component.ui.onboarding;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.AuthenticationManager;
import de.tum.in.tumcampusapp.api.tumonline.AccessTokenManager;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.component.tumui.person.model.Faculty;
import de.tum.in.tumcampusapp.component.ui.survey.SurveyManager;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Displays the first page of the startup wizard, where the user can enter his lrz-id.
 */
public class WizNavStartActivity extends ActivityForLoadingInBackground<String, Boolean> implements OnClickListener {
    private final AccessTokenManager accessTokenManager = new AccessTokenManager(this);
    private EditText editTxtLrzId;
    private String lrzId;

    public WizNavStartActivity() {
        super(R.layout.activity_wiznav_start);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disableRefresh();
        findViewById(R.id.wizard_start_layout).requestFocus();

        setUpSpinner(); // Faculty selector

        editTxtLrzId = findViewById(R.id.lrd_id);
        editTxtLrzId.setText(Utils.getSetting(this, Const.LRZ_ID, ""));
    }

    public void setUpSpinner() {
        final Spinner userMajorSpinner = findViewById(R.id.majorSpinner);

        new AsyncTask<Void, Void, String[]>() {

            // fetch facultyData from API
            @Override
            protected String[] doInBackground(Void... voids) {
                ArrayList<String> fetchedFaculties = new ArrayList<>();
                SurveyManager sm = new SurveyManager(getApplicationContext());
                sm.downloadFacultiesFromExternal();

                List<Faculty> faculties = sm.getAllFaculties();
                for (Faculty faculty : faculties) {
                    fetchedFaculties.add(faculty.getName());
                }

                fetchedFaculties.add(0, getResources().getString(R.string.choose_own_faculty));
                return fetchedFaculties.toArray(new String[fetchedFaculties.size()]);
            }

            // Fill the fetched facultyData into the majorSpinner
            @SuppressLint("ShowToast")
            @Override
            protected void onPostExecute(String[] majors) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, majors);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userMajorSpinner.setAdapter(adapter);

                Utils.setInternalSetting(getApplicationContext(), "user_major", "0"); // Prior to faculty selection, the user has major 0 (which means) All faculties for faculty match in card
                userMajorSpinner.setSelection(Integer.parseInt(Utils.getInternalSettingString(getApplicationContext(), "user_faculty_number", "0")));

                // Upon clicking on the faculty spinner and there is no internet connection -> toast to the user.
                userMajorSpinner.setOnTouchListener((view, motionEvent) -> {
                    if (!NetUtils.isConnected(getApplicationContext())) {
                        Toast.makeText(getApplicationContext(), getString(R.string.please_connect_to_internet), Toast.LENGTH_LONG)
                             .show();
                    }
                    return view.performClick();
                });

                // When the user chooses a faculty
                userMajorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        SurveyManager sm = new SurveyManager(getApplicationContext());
                        String id = sm.getFacultyID((String) adapterView.getItemAtPosition(i));
                        if (id != null) {
                            Utils.setInternalSetting(getApplicationContext(), "user_major", id); // save faculty number in shared preferences
                            setDefaultCampus(id);
                            Utils.setInternalSetting(getApplicationContext(), "user_faculty_number", String.valueOf(userMajorSpinner.getSelectedItemPosition())); // save choosen spinner poistion so that in case the user returns from the  WizNavCheckTokenActivity to WizNavStart activity, then we the faculty gets autm. choosen.
                        }
                        TextView selectedItem = (TextView) adapterView.getChildAt(0);
                        if (selectedItem != null) {
                            selectedItem.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_primary)); // set the colour of the selected item in the faculty spinner
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        // NOOP
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
        // Upon clicking on the skip button and there is no internet connection -> toast to the user
        if (!NetUtils.isConnected(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_connect_to_internet), Toast.LENGTH_LONG)
                 .show();
            return;
        }

        finish();
        startActivity(new Intent(this, WizNavExtrasActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * Handle click on next button
     *
     * @param next Next button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickNext(View next) {
        // Upon clicking on next button and there is no internet connection -> toast to the user.
        if (!NetUtils.isConnected(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_connect_to_internet), Toast.LENGTH_LONG)
                 .show();
            return;
        }

        String enteredId = editTxtLrzId.getText()
                                       .toString()
                                       .toLowerCase();

        // check if lrz could be valid?
        if (!enteredId.matches(Const.TUM_ID_PATTERN)) {
            Utils.showToast(this, R.string.error_lrz_wrong);
            return;
        }

        lrzId = enteredId;
        Utils.setSetting(this, Const.LRZ_ID, lrzId);

        // is access token already set?
        if (accessTokenManager.hasValidAccessToken()) {
            // show Dialog first
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.dialog_new_token))
                    .setPositiveButton(getString(R.string.yes), this)
                    .setNegativeButton(getString(R.string.no), this)
                    .show();
        } else {
            startLoading(lrzId);
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
            AuthenticationManager am = new AuthenticationManager(this);
            am.clearKeys();
            am.generatePrivateKey(null);
            startLoading(lrzId);
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            onLoadFinished(true);
        }
    }

    private void setDefaultCampus(String facultyNumber) {
        String Campus = "0";
        switch (facultyNumber) {
            case "5":   // TUM School of Education
            case "6":   // Architektur
            case "7":   // Elektrotechnik und Informationstechnik
            case "8":   // Ingenieurfakultät Bau Geo Umwelt
            case "14":  // Wirtschaftswissenschaften
            case "17":  // Andere Einrichtungen
                Campus = "C"; // Stammgelände
                break;

            case "16":  // TUM School of Governance
                // Unklar, nicht weit vom Stammgelände, aber nicht Stammgelände ??
                break;
            case "1":   // Mathematik
            case "2":   // Physik
            case "3":   // Chemie
            case "4":   // Informatik
            case "11":  // Maschinenwesen
                Campus = "G"; // Garching-FZ
                break;

            case "13":  // Sport-und Gesundheitswissenschaften
                // Olympiapark, hat aber keine Zuordnung ??
                break;

            case "12":  // Medizin
                Campus = "I"; // Klinikum rechts der Isar
                break;

            case "15":  // Wissenschaftszentrum Weihenstephan
                Campus = "W"; // Weihenstephan
                break;
            default:
                break;

        }

        if ("0".equals(Campus)) {
            Utils.setSetting(getApplicationContext(), Const.DEFAULT_CAMPUS, Campus);
        }
    }

    /**
     * Requests an access-token from the TumOnline server in background
     *
     * @param arg Unused
     * @return True if the access token was successfully created
     */
    @Override
    protected Boolean onLoadInBackground(String... arg) {
        return accessTokenManager.requestAccessToken(this, arg[0]);
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
