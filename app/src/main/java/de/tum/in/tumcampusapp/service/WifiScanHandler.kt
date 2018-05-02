package de.tum.`in`.tumcampusapp.service

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import de.tum.`in`.tumcampusapp.utils.Utils
import java.util.*

/**
 * This class is responsible for starting repeating wifi scans.
 * Its next schedule will be called at random. The upper and lower bound
 * for the next schedule are defined by MIN_ AND MAX_TIME_PASSED_IN_SECONDS.
 * The generator then randomly picks a number in this range as input for postdelay
 */

class WifiScanHandler : Handler() {

    private var periodicalScan: PeriodicalScan? = null

    fun startRepetition() {
        val interval = generateRandomScanInterval(MIN_TIME_PASSED_IN_SECONDS, MAX_TIME_PASSED_IN_SECONDS - MIN_TIME_PASSED_IN_SECONDS)
        postDelayed(periodicalScan, interval.toLong())
    }

    private class PeriodicalScan(val context: Context? = null) : Runnable {

        override fun run() {
            context?.let { ctx ->
                val wifiManager = ctx.applicationContext
                        .getSystemService(Context.WIFI_SERVICE) as WifiManager
                wifiManager.startScan()
                Utils.log("WifiScanHandler started")
            }
        }

    }

    companion object {

        private val INSTANCE = WifiScanHandler()

        //Big range advised, e.g. 10s to 420s (7min), since there's a possibility for battery drain
        private val MIN_TIME_PASSED_IN_SECONDS = 5
        private val MAX_TIME_PASSED_IN_SECONDS = 420

        fun getInstance(context: Context) = INSTANCE.apply {
            periodicalScan = PeriodicalScan(context)
            removeCallbacksAndMessages(null)
        }

        private fun generateRandomScanInterval(minimumSeconds: Int, range: Int): Int {
            val random = Random()
            return 1000 * minimumSeconds + random.nextInt(range * 1000)
        }

    }

}
