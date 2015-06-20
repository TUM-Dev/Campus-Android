package de.tum.in.tumcampus.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.MoodleManager;
import de.tum.in.tumcampus.models.managers.MoodleUpdateDelegate;
import de.tum.in.tumcampus.models.managers.RealMoodleManager;

/**
 * Created by carlodidomenico on 16/06/15.
 */
public class MoodleLoginActivity extends ActivityForDownloadingExternal implements MoodleUpdateDelegate, View.OnClickListener {

    protected MoodleManager realManager;

    //ProgressDialog for loading
    private ProgressDialog mDialog;
    private EditText userNameField, passwordField;
    private Button button;
    private Intent intent;

    public MoodleLoginActivity() {
        super("moodle_login", R.layout.activity_moodle_login);
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        baseSetup();
       initialiseNextIntent();
    }

    public void initialiseNextIntent(){
        try {

            if ((boolean) getIntent().getExtras().get("outside_activity")) {
                Utils.log("Login started from moodle manager");
                intent = null;
            } else {
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
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        realManager = RealMoodleManager.getInstance(this, this);
        String user = userNameField.getText().toString();
        String pass = passwordField.getText().toString();
        Utils.log("user name " + user + " pass" + pass);
        realManager.requestUserToken(this, user, pass);
    }


    @Override
    public void refresh() {
        Utils.log("getting called by moodle manager");
        if (realManager.getToken() == null)
            Utils.showToast(this, R.string.login_failed);
        else {
            // do nothing ! not needed for this class
            if (intent != null) {
                startActivity(intent);
                Utils.log("got the token now starting the previous activity");
            }
            finish();
        }
    }
}
