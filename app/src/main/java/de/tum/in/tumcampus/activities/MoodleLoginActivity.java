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
        Class<?> previousClass = (Class<?>)getIntent().getExtras().get("class_name");
        if (previousClass == null) {
            Utils.log("Warn! previous class was null!");
            previousClass = MoodleMainActivity.class;
        }
        intent = new Intent(this, previousClass);
    }

    public Intent getIntentForPreviousActivity( String className){
        String packageName = getPackageName();
        try {
            Class<?> c = Class.forName(packageName + "." + className);
            return new Intent(this, c);
        }catch (ClassNotFoundException exception){
            Utils.log("#Error! could not find class name " + className
            + " in package " + packageName);
            return null;
        }
    }

    public void setUp(){
        realManager = RealMoodleManager.getInstance(this, this);
        userNameField = (EditText)findViewById(R.id.user_name);
        passwordField = (EditText)findViewById(R.id.password);
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        String user = userNameField.getText().toString();
        String pass = userNameField.getText().toString();
        realManager.requestUserToken(this, user, pass);
    }


    @Override
    public void refresh() {
        if (realManager.getToken() == null)
            Utils.showToast(this, R.string.login_failed);
        else {
            // do nothing ! not needed for this class
            startActivity(intent);
            finish();
        }
    }
}
