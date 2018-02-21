package de.tum.`in`.tumcampusapp.component.ui.alarm

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.ui.alarm.model.GCMNotification
import de.tum.`in`.tumcampusapp.utils.Utils

/**
 * Activity to show any alarms
 */
class AlarmActivity : BaseActivity(R.layout.activity_alarmdetails) {

    private lateinit var mTitle: TextView
    private lateinit var mDescription: WebView
    private lateinit var mDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.mTitle = findViewById(R.id.alarm_title)
        this.mDescription = findViewById(R.id.alarm_description)
        this.mDate = findViewById(R.id.alarm_date)

        this.processIntent(intent)
    }

    override fun onNewIntent(intent: Intent) = this.processIntent(intent)

    private fun processIntent(intent: Intent) {
        val notification = intent.getSerializableExtra("info") as GCMNotification
        //GCMAlert alert = (GCMAlert) intent.getSerializableExtra("alert"); //Currently only has the silent flag, don't need it atm

        Utils.log(notification.toString())

        this.mTitle.text = notification.title
        this.mDescription.loadDataWithBaseURL(null, notification.description, "text/html", "utf-8", null)
        this.mDescription.setBackgroundColor(Color.TRANSPARENT)
        this.mDate.text = notification.created
    }
}
