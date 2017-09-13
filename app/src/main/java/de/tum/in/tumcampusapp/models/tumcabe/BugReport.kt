package de.tum.`in`.tumcampusapp.models.tumcabe

import android.content.Context

import de.tum.`in`.tumcampusapp.auxiliary.NetUtils
import de.tum.`in`.tumcampusapp.trace.G
import de.tum.`in`.tumcampusapp.trace.Util

class BugReport(c: Context, private val stacktrace: String, private val log: String) {

    private val packageName: String
    private val packageVersion: String
    private val packageVersionCode: String
    private val phoneModel: String
    private val androidVersion: String
    private val networkWifi: String
    private val networkMobile: String
    private val gps: String
    private val screenWidth: String
    private val screenHeight: String
    private val screenOrientation: String
    private val screenDpi: String


    init {
        this.packageName = G.appPackage
        this.packageVersion = G.appVersion
        this.packageVersionCode = G.appVersionCode.toString()

        this.phoneModel = G.phoneModel
        this.androidVersion = G.androidVersion

        this.networkWifi = if (NetUtils.isConnectedWifi(c)) "true" else "false"
        this.networkMobile = if (NetUtils.isConnectedMobileData(c)) "true" else "false"
        this.gps = Util.isGPSOn(c)

        val screenProperties = Util.getScreenProperties(c)
        this.screenWidth = screenProperties[0]
        this.screenHeight = screenProperties[1]
        this.screenOrientation = screenProperties[2]
        this.screenDpi = screenProperties[3] + ":" + screenProperties[4]
    }
}
