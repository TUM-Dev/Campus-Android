package de.tum.in.tumcampusapp.activities;

import de.tum.in.tumcampusapp.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Toast;
;

public class WizNavDoneActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {

		   super.onCreate(savedInstanceState);
		   setContentView(R.layout.activity_wiznavdone);
	    }

	 public void onClickDone(View view){
	     Intent strtActivity=new Intent(this,StartActivity.class);
	     startActivity(strtActivity);
	 }
}
