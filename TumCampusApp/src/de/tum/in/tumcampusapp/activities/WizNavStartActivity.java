package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.WizAccessTokenManager;
import de.tum.in.tumcampusapp.services.ImportService;

public class WizNavStartActivity extends Activity {
	private WizAccessTokenManager accessTokenManager = new WizAccessTokenManager(this);
	
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_wiznavstart);
	        
	       
	    }
	 
	 public void onClickNext(View view){
		 EditText editText=(EditText)findViewById(R.id.lrd_id);
		 String lrz_id=editText.getText().toString();
		
	     accessTokenManager.setupAccessToken(lrz_id);
	     
	     if(accessTokenManager.isFine){
	    	 SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		     Editor editor = sp.edit();
		     editor.putString("lrz_id", lrz_id);
		     editor.commit();
	    	 Intent wizNav=new Intent(this,WizNavNextActivity.class);
	    	 startActivity(wizNav);
	     }
	     else{
	    	 TextView tv=(TextView)findViewById(R.id.textViewErr);
	    	 tv.setText(accessTokenManager.message);
	    	 //editText=(EditText)findViewById()
	     }
	 }
	 public void onClickClose(View view){
		 Intent startAct=new Intent(this,StartActivity.class);
		 startActivity(startAct);
	 }
}
