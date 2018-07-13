package de.tum.`in`.tumcampusapp.component.ui.overview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import de.psdev.licensesdialog.LicensesDialog
import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.activity_information.*

/**
 * Provides information about this app and all contributors
 */
class InformationActivity : BaseActivity(R.layout.activity_information) {

    private val rowParams = TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.displayDebugInfo()

        button_facebook.setOnClickListener {
            openFacebook();
        }
        button_github.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_link))))
        }
        button_privacy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_privacy_policy))))
        }
        button_chat_terms.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_chat_terms))))
        }
        button_licenses.setOnClickListener {
            LicensesDialog.Builder(this)
                    .setNotices(R.raw.notices)
                    .setShowFullLicenseText(false)
                    .setIncludeOwnLicense(true)
                    .build()
                    .show()
        }
    }

    private fun openFacebook() {
        // Open the facebook app or view in a browser when not installed
        var facebook: Intent
        try {
            //Try to get facebook package to check if fb app is installed
            getPackageManager().getPackageInfo("com.facebook.katana", 0)
            facebook = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_link_app)))
        } catch (e: PackageManager.NameNotFoundException) {
            //otherwise just open the normal url
            facebook = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_link)))
        }

        startActivity(facebook)
    }

    private fun displayDebugInfo() {
        //Setup showing of debug information
        val sp = PreferenceManager.getDefaultSharedPreferences(this)

        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            this.addDebugRow(debugInfos, "App Version", packageInfo.versionName)
        } catch (ignore: NameNotFoundException) {
        }
        this.addDebugRow(debugInfos, "TUM ID", sp.getString(Const.LRZ_ID, ""))
        val token = sp.getString(Const.ACCESS_TOKEN, "")
        if(token == ""){
            this.addDebugRow(debugInfos, "TUM Access token", "")
        } else {
            this.addDebugRow(debugInfos, "TUM Access token", token.substring(0, 5) + "...")
        }
        this.addDebugRow(debugInfos, "Bugreports", sp.getBoolean(Const.BUG_REPORTS, false).toString() + " ")

        this.addDebugRow(debugInfos, "REG ID", Utils.getSetting(this, Const.FCM_REG_ID, ""))
        this.addDebugRow(debugInfos, "REG Transmission", DateUtils.getRelativeDateTimeString(this,
                Utils.getSettingLong(this, Const.FCM_REG_ID_LAST_TRANSMISSION, 0),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS * 2, 0).toString())
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            this.addDebugRow(debugInfos, "VersionCode", packageInfo.versionCode.toString())
        } catch (ignore: NameNotFoundException) {
        }

        this.addDebugRow(debugInfos, "BuildConfig, Debug = ", BuildConfig.DEBUG.toString())

        debugInfos.visibility = View.VISIBLE
    }

    private fun addDebugRow(t: TableLayout, label: String, value: String?) {
        //Create new row
        val tableRow = TableRow(this)
        tableRow.layoutParams = rowParams

        //Add our text fields
        val l = TextView(this)
        l.text = label
        l.layoutParams = rowParams
        l.setPadding(0,0,25,0)
        tableRow.addView(l)

        val v = TextView(this)
        v.text = value
        v.layoutParams = rowParams
        v.isClickable = true

        //Copy to clipboard
        v.setOnClickListener { v1 ->
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, value)
            clipboard.primaryClip = clip
        }
        tableRow.addView(v)

        //Add it to the table
        t.addView(tableRow)
    }
}
