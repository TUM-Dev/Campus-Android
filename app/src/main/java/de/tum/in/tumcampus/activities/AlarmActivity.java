package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.activities.generic.BaseActivity;
import de.tum.in.tumcampus.adapters.KinoAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.GCMAlert;
import de.tum.in.tumcampus.models.GCMNotification;
import de.tum.in.tumcampus.models.managers.KinoManager;

/**
 * Activity to show any alarms
 */
public class AlarmActivity extends BaseActivity {

    private TextView mTitle;
    private WebView mDescription;
    private TextView mDate;

    public AlarmActivity() {
        super(R.layout.activity_alarmdetails);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mTitle = (TextView) findViewById(R.id.alarm_title);
        this.mDescription = (WebView) findViewById(R.id.alarm_description);
        this.mDate = (TextView) findViewById(R.id.alarm_date);

        this.processIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.processIntent(intent);
    }

    private void processIntent(Intent intent) {
        GCMNotification notification = (GCMNotification) intent.getSerializableExtra("info");
        GCMAlert alert = (GCMAlert) intent.getSerializableExtra("alert"); //Currently only has the silent flag, don't need it atm

        Utils.log(notification.toString());

        this.mTitle.setText(notification.getTitle());
        this.mDescription.loadDataWithBaseURL(null, notification.getDescription(), "text/html", "utf-8", null);
        this.mDescription.setBackgroundColor(Color.TRANSPARENT);
        this.mDate.setText(notification.getCreated());

    }

}

