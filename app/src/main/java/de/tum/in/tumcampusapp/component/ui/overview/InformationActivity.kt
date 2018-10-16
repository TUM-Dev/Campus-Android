package de.tum.`in`.tumcampusapp.component.ui.overview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.pm.PackageInfoCompat
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
            openFacebook()
        }
        button_github.setOnClickListener {
            startActivity(Intent(ACTION_VIEW, Uri.parse(getString(R.string.github_link))))
        }
        button_privacy.setOnClickListener {
            startActivity(Intent(ACTION_VIEW, Uri.parse(getString(R.string.url_privacy_policy))))
        }
        button_chat_terms.setOnClickListener {
            startActivity(Intent(ACTION_VIEW, Uri.parse(getString(R.string.url_chat_terms))))
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

    /**
     * Open the Facebook app or view page in a browser if Facebook is not installed.
     */
    private fun openFacebook() {
        try {
            packageManager.getPackageInfo("com.facebook.katana", 0)
            val intent = Intent(ACTION_VIEW, Uri.parse(getString(R.string.facebook_link_app)))
            startActivity(intent)
        } catch (e: Exception) {
            // Don't make any assumptions about another app, just start the browser instead
            val default = Intent(ACTION_VIEW, Uri.parse(getString(R.string.facebook_link)))
            startActivity(default)
        }
    }

    private fun displayDebugInfo() {
        //Setup showing of debug information
        val sp = PreferenceManager.getDefaultSharedPreferences(this)

        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            this.addDebugRow(debugInfos, "App version", packageInfo.versionName)
        } catch (ignore: NameNotFoundException) {
        }

        this.addDebugRow(debugInfos, "TUM ID", sp.getString(Const.LRZ_ID, ""))
        val token = sp.getString(Const.ACCESS_TOKEN, "")
        if (token == "") {
            this.addDebugRow(debugInfos, "TUM access token", "")
        } else {
            this.addDebugRow(debugInfos, "TUM access token", token?.substring(0, 5) + "...")
        }
        this.addDebugRow(debugInfos, "Bug reports", sp.getBoolean(Const.BUG_REPORTS, false).toString() + " ")

        this.addDebugRow(debugInfos, "REG ID", Utils.getSetting(this, Const.FCM_REG_ID, ""))
        this.addDebugRow(debugInfos, "REG transmission", DateUtils.getRelativeDateTimeString(this,
                Utils.getSettingLong(this, Const.FCM_REG_ID_LAST_TRANSMISSION, 0),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS * 2, 0).toString())
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            this.addDebugRow(debugInfos, "Version code", PackageInfoCompat.getLongVersionCode(packageInfo).toString())
        } catch (ignore: NameNotFoundException) {
        }

        this.addDebugRow(debugInfos, "Build configuration", BuildConfig.DEBUG.toString())

        debugInfos.visibility = View.VISIBLE
    }

    private fun addDebugRow(tableLayout: TableLayout, label: String, value: String?) {
        val tableRow = TableRow(this)
        tableRow.layoutParams = rowParams

        val labelTextView = TextView(this).apply {
            text = label
            layoutParams = rowParams

            val padding = resources.getDimensionPixelSize(R.dimen.material_small_padding)
            setPadding(0, 0, padding, 0)
        }
        tableRow.addView(labelTextView)

        val valueTextView = TextView(this).apply {
            text = value
            layoutParams = rowParams
            isClickable = true

            setOnLongClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(label, value)
                clipboard.primaryClip = clip
                true
            }
        }
        tableRow.addView(valueTextView)

        tableLayout.addView(tableRow)
    }
}
