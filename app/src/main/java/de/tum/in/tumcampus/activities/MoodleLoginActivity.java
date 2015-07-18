package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ProgressActivity;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.MoodleManager;
import de.tum.in.tumcampus.models.managers.MoodleUpdateDelegate;
import de.tum.in.tumcampus.models.managers.RealMoodleManager;

/**
 * Activity to log into moodle
 */
public class MoodleLoginActivity extends ProgressActivity implements MoodleUpdateDelegate, View.OnClickListener {

    protected MoodleManager realManager;

    private EditText userNameField, passwordField;
    private Intent intent;

    public MoodleLoginActivity() {
        super(R.layout.activity_moodle_login);
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        baseSetup();
       initialiseNextIntent();
    }

    @Override
    public void onRefresh() {
        showLoadingEnded();
    }

    public void initialiseNextIntent(){
        try {

            if ((boolean) getIntent().getExtras().get("outside_activity")) {
                // when login request is called by moodleManager not an activity
                // no need to start the previous activity. (The previous activity
                // is still alive!)
                intent = null;
            } else {
                // previuos class is used to be launched when the login was successful
                Class<?> previousClass = (Class<?>) getIntent().getExtras().get("class");
                if (previousClass == null) {
                    Utils.log("Warn! previous class was null!");
                    previousClass = MoodleMainActivity.class;
                }

                Utils.log("Intent set to start class " + previousClass.getName());
                intent = new Intent(this, previousClass);
            }
        }catch (Exception e) {
            Utils.log(e, "Error! could not get the class form intent");
        }
    }

    public void baseSetup(){
        userNameField = (EditText)findViewById(R.id.user_name);
        passwordField = (EditText)findViewById(R.id.password);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        realManager = RealMoodleManager.getInstance(this, this);
        String user = userNameField.getText().toString();
        String pass = passwordField.getText().toString();
        Utils.log("user name " + user + " pass" + pass);
        realManager.requestUserToken(this, user, pass);
        showLoadingStart();
    }


    /**
     It is called by Moodlemanager when the requested data
     * is ready. In this case after requesting requestUserData() after the first login
     * attempt
     */
    @Override
    public void refresh() {

        if (realManager.getToken() == null) {
            // if still token is not valid
            showLoadingEnded();
            if (! NetUtils.isConnected(this))
                showNoInternetLayout();
            else
                Utils.showToast(this, R.string.login_failed);
        }
        else {
            if (intent != null) {
                startActivity(intent);
                Utils.log("got the token now starting the previous activity");
            }
        }
    }
}
