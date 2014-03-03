package de.tum.in.tumcampusapp.activities;

import com.actionbarsherlock.app.SherlockActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Displays Events which are fetched from Facebook
 * 
 * @author Sascha Moecker
 * 
 */
public class ImplicitCounter extends SherlockActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
	}
	public static boolean Counter(String key,Context context)
	{
		boolean myboolean=true;
		//Counting number of the times that the user used this activity.
				 SharedPreferences sp = context.getSharedPreferences("MyPrefrence", Context.MODE_PRIVATE);
				int myvalue = sp.getInt(key,0);
				myvalue=myvalue+1;
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt(key,myvalue);
				editor.commit();
				////

				 int myIntValue = sp.getInt(key,0);
				 if(myIntValue==5){
					    SharedPreferences sharedPrefs =PreferenceManager.getDefaultSharedPreferences(context);
						SharedPreferences.Editor editor1 = sharedPrefs.edit();
						editor1.putBoolean(key, true);
						editor1.commit();
						editor.putInt(key,0);
						editor.commit();

				 }
		return myboolean;
				
		
	}


	@Override
	protected void onResume() {
		super.onResume();
	
	}

	@Override
	protected void onStart() {
		super.onStart();

		
	}
}
