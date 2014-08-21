package de.tum.in.tumcampus.auxiliary;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Counts the usage of specific app functions
 *
 * @author Sascha Moecker
 */
public class ImplicitCounter {

    public static boolean Counter(String key, Context context) {
        boolean myboolean = true;
        //Counting number of the times that the user used this activity.
        SharedPreferences sp = context.getSharedPreferences("MyPrefrence", Context.MODE_PRIVATE);
        int myvalue = sp.getInt(key, 0);
        myvalue = myvalue + 1;
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, myvalue);
        editor.commit();
        ////

        int myIntValue = sp.getInt(key, 0);
        if (myIntValue == 5) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor1 = sharedPrefs.edit();
            editor1.putBoolean(key, true);
            editor1.commit();
            editor.putInt(key, 0);
            editor.commit();

        }
        return myboolean;
    }
}
