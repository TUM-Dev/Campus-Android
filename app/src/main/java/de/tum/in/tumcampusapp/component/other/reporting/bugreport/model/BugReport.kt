package de.tum.`in`.tumcampusapp.component.other.reporting.bugreport.model

import android.content.Context
import de.tum.`in`.tumcampusapp.component.other.reporting.bugreport.G
import de.tum.`in`.tumcampusapp.component.other.reporting.bugreport.Util
import de.tum.`in`.tumcampusapp.utils.NetUtils

data class BugReport(var packageName: String = "",
                     var packageVersion: String = "",
                     var packageVersionCode: String = "",
                     var phoneModel: String = "",
                     var androidVersion: String = "",
                     var networkWifi: String = "",
                     var networkMobile: String = "",
                     var gps: String = "",
                     var screenWidth: String = "",
                     var screenHeight: String = "",
                     var screenOrientation: String = "",
                     var screenDpi: String = "",
                     var log: String = "",
                     var stacktrace: String = "") {

    companion object {
        fun getBugReport(c: Context, stacktrace: String, log: String): BugReport {
            val screenProperties = Util.getScreenProperties(c)
            return BugReport(
                    packageName = G.appPackage,
                    packageVersion = G.appVersion,
                    packageVersionCode = G.appVersionCode.toString(),
                    phoneModel = G.phoneModel,
                    androidVersion = G.androidVersion,
                    networkWifi = NetUtils.isConnectedWifi(c).toString(),
                    networkMobile = NetUtils.isConnectedMobileData(c).toString(),
                    gps = Util.isGPSOn(c),
                    screenWidth = screenProperties[0],
                    screenHeight = screenProperties[1],
                    screenOrientation = screenProperties[2],
                    screenDpi = "${screenProperties[3]}:${screenProperties[4]}",
                    stacktrace = stacktrace,
                    log = log
            )
        }
    }
}
