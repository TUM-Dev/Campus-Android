package de.tum.in.tumcampus.activities;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Provides information about this app and all contributors
 */
public class InformationActivity extends AppCompatActivity {

    private TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
    private int debugOptionsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
        setContentView(R.layout.activity_information);

        this.displayVersionName();
    }

    /**
     * Display version name
     */
    private void displayVersionName() {

        //Get the information
        String versionName = "";
        PackageInfo packageInfo;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = getResources().getString(R.string.version) + ": " + packageInfo.versionName;
        } catch (NameNotFoundException e) {
            Utils.log(e);
        }

        //Set it up on the ui
        TextView tv = (TextView) findViewById(R.id.txt_version);
        tv.setText(versionName);

        //Setup showing of debug information
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Lock it after five clicks
                if (debugOptionsCount > 5) {
                    return;
                }

                //Increase
                debugOptionsCount++;

                //Show at five clicks
                if (debugOptionsCount == 5) {
                    InformationActivity.this.displayDebugInfo();
                }
            }

        });
    }

    private void displayDebugInfo() {

        TableLayout table = (TableLayout) findViewById(R.id.debugInfos);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        this.addDebugRow(table, "LRZ ID", sp.getString(Const.LRZ_ID, ""));
        this.addDebugRow(table, "TUM Access token", sp.getString(Const.ACCESS_TOKEN, ""));
        this.addDebugRow(table, "Bugreports", sp.getBoolean(Const.BUG_REPORTS, false) + " ");
        this.addDebugRow(table, "REG ID", Utils.getInternalSettingString(this, Const.GCM_REG_ID, ""));
        this.addDebugRow(table, "REG Transmission", DateUtils.getRelativeDateTimeString(this, Utils.getInternalSettingLong(this, Const.GCM_REG_ID_LAST_TRANSMISSION, 0),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS * 2, 0).toString());

        table.setVisibility(View.VISIBLE);

    }

    private void addDebugRow(TableLayout t, final String label, final String value) {
        //Create new row
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(rowParams);

        //Add our text fields
        TextView l = new TextView(this);
        l.setText(label);
        l.setLayoutParams(rowParams);
        tableRow.addView(l);

        TextView v = new TextView(this);
        v.setText(value);
        v.setLayoutParams(rowParams);
        v.setClickable(true);

        //Copy to clipboard
        v.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    @SuppressWarnings("deprecation")
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setText(value);
                } else {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(label, value);
                    clipboard.setPrimaryClip(clip);
                }
            }
        });
        tableRow.addView(v);

        //Add it to the table
        t.addView(tableRow);
    }
}
