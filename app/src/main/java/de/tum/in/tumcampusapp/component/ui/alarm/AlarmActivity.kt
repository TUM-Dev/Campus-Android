package de.tum.`in`.tumcampusapp.component.ui.alarm

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.ui.alarm.model.FcmNotification

import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
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
        val notification = intent.getSerializableExtra("info") as FcmNotification
        // FcmAlert alert = (FcmAlert) intent.getSerializableExtra("alert"); //Currently only has the silent flag, don't need it atm

        Utils.log(notification.toString())

        this.mTitle.text = notification.title
        this.mDescription.loadDataWithBaseURL(null, formatDescription(notification.description), "text/html", "utf-8", null)
        this.mDescription.setBackgroundColor(Color.TRANSPARENT)
        this.mDate.text = DateTimeUtils.getDateString(notification.created)
    }

    private fun formatDescription(description: String): String {
        val color = ContextCompat.getColor(this, R.color.text_primary)
        val hexColor = "#" + String.format("%06X", color and 0x00ffffff)

        val preHTML = "<!DOCTYPE HTML>\n" +
                "<html>\n" +
                "<head>\n" +
                "<style type=\"text/css\">\n" +
                "body{color: " + hexColor + ";}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n"

        val postHTML = "\n</body>\n" +
                "</html>"

        return preHTML + description + postHTML
    }
}
