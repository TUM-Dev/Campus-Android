package de.tum.in.tumcampusapp.component.ui.overview;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.other.reporting.stats.ImplicitCounter;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Provides information about this app and all contributors
 */
public class InformationActivity extends BaseActivity {

    private final TableRow.LayoutParams rowParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private int debugOptionsCount;

    public InformationActivity() {
        super(R.layout.activity_information);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImplicitCounter.count(this);
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
        TextView tv = findViewById(R.id.txt_version);
        tv.setText(versionName);

        //Setup showing of debug information
        tv.setOnClickListener(v -> {
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
        });
    }

    private void displayDebugInfo() {

        TableLayout table = findViewById(R.id.debugInfos);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        this.addDebugRow(table, "LRZ ID", sp.getString(Const.LRZ_ID, ""));
        this.addDebugRow(table, "TUM Access token", sp.getString(Const.ACCESS_TOKEN, ""));
        this.addDebugRow(table, "Bugreports", sp.getBoolean(Const.BUG_REPORTS, false) + " ");
        this.addDebugRow(table, "REG ID", Utils.getInternalSettingString(this, Const.GCM_REG_ID, ""));
        this.addDebugRow(table, "REG Transmission", DateUtils.getRelativeDateTimeString(this, Utils.getInternalSettingLong(this, Const.GCM_REG_ID_LAST_TRANSMISSION, 0),
                                                                                        DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS * 2, 0)
                                                             .toString());
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            this.addDebugRow(table, "VersionCode", String.valueOf(packageInfo.versionCode));
        } catch (NameNotFoundException e) {
            Utils.log(e);
        }

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
        v.setOnClickListener(v1 -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(label, value);
            clipboard.setPrimaryClip(clip);
        });
        tableRow.addView(v);

        //Add it to the table
        t.addView(tableRow);
    }
}
