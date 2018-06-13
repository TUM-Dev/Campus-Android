package de.tum.`in`.tumcampusapp.service

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import de.tum.`in`.tumcampusapp.utils.Utils
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class is responsible for starting repeating wifi scans.
 * Its next schedule will be called at random. The upper and lower bound
 * for the next schedule are defined by MIN_ AND MAX_TIME_PASSED_IN_SECONDS.
 * The generator then randomly picks a number in this range as input for postDelayed
 */

class WifiScanHandler : Handler() {
    private var isRunning = AtomicBoolean(false)
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiLock: WifiManager.WifiLock
    private val periodicalScan = {
        wifiLock.acquire()
        wifiManager.startScan()
        Utils.log("WifiScanHandler started")
    }

    fun startRepetition(context: Context) {
        if (!isRunning.compareAndSet(false, true)) {
            return
        }
        wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "WifiScanHandler")
        val interval = generateRandomScanInterval(MIN_TIME_PASSED_IN_SECONDS, MAX_TIME_PASSED_IN_SECONDS - MIN_TIME_PASSED_IN_SECONDS)
        postDelayed(periodicalScan, interval.toLong())
    }

    fun onScanFinished() {
        if (wifiLock.isHeld) {
            wifiLock.release()
        }
    }

    companion object {
        private val INSTANCE = WifiScanHandler()

        //Big range advised, e.g. 10s to 420s (7min), since there's a possibility for battery drain
        private const val MIN_TIME_PASSED_IN_SECONDS = 5
        private const val MAX_TIME_PASSED_IN_SECONDS = 420

        fun getInstance() = INSTANCE.apply {
            removeCallbacksAndMessages(null)
        }

        private fun generateRandomScanInterval(minimumSeconds: Int, range: Int): Int {
            val random = Random()
            return 1000 * minimumSeconds + random.nextInt(range * 1000)
        }

    }

}
