package de.tum.in.tumcampusapp.activities;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.WizAccessTokenManager;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WizNavNextActivity extends ActivityForAccessingTumOnline {
	public WizNavNextActivity() {
		  super("isTokenConfirmed", R.layout.activity_wiznavnext);
	}
	public void onCreate(Bundle savedInstanceState) {
		 
		   super.onCreate(savedInstanceState);
	       // super.onCreate(savedInstanceState);
	    }
	
	 protected void onStart(){
		 super.onStart();
		 TextView textView =(TextView)findViewById(R.id.tvBrowse);
	        textView.setClickable(true);
	        textView.setMovementMethod(LinkMovementMethod.getInstance());
	        String text = "<a href='http://campus.tum.de'> Enable Token through TUM campus portal using Token-Management </a>";
	        textView.setText(Html.fromHtml(text));
	 }

	 public void onClickNext(View view){
		
		 super.requestFetch();
	 }
	 
	 public void onClickClose(View view){
		 
		 
		 Intent startAct=new Intent(this,StartActivity.class);
		 startActivity(startAct);
	 }
	
	@Override
	public void onFetch(String rawResponse) {
		Log.d("Wizard response:",rawResponse);
		if(rawResponse.contains("true")){
			this.finishAffinity();
			Intent wiz2=new Intent(this,WizNavDoneActivity.class);
			startActivity(wiz2);
			
		}
		else if(rawResponse.contains("false")){
			Toast.makeText(this, "Token is not enabled", Toast.LENGTH_LONG).show();
		}
		else{
			
			Toast.makeText(this, "Please check your internet connectivity", Toast.LENGTH_LONG).show();
		}

		progressLayout.setVisibility(View.GONE);
	}

}
