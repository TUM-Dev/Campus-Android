package de.tum.in.tumcampusapp.services;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;

import java.util.Random;

import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * This class is responsible for starting repeating wifi scans.
 * Its next schedule will be called at random. The upper and lower bound
 * for the next schedule are defined by MIN_ AND MAX_TIME_PASSED_IN_SECONDS.
 * The generator then randomly picks a number in this range as input for postdelay
 */

public class WifiScanHandler extends Handler {
    private static final WifiScanHandler singleton = new WifiScanHandler();
    private static PeriodicalScan periodicalScan;
    private static Context context;

    //Big range advised, e.g. 10s to 420s (7min), since there's a possibility for battery drain
    private static final int MIN_TIME_PASSED_IN_SECONDS = 5;
    private static final int MAX_TIME_PASSED_IN_SECONDS = 420;

    private WifiScanHandler() {
    }

    public static WifiScanHandler getInstance(Context context) {
        WifiScanHandler.context = context;
        singleton.removeCallbacksAndMessages(null);
        periodicalScan = new PeriodicalScan();
        return singleton;
    }

    public void startRepetition() {
        this.postDelayed(periodicalScan, generateRandomScanInterval(MIN_TIME_PASSED_IN_SECONDS, MAX_TIME_PASSED_IN_SECONDS - MIN_TIME_PASSED_IN_SECONDS));
    }

    private static int generateRandomScanInterval(int minimumSeconds, int range) {
        Random random = new Random();
        return 1000 * minimumSeconds + random.nextInt(range * 1000);
    }

    private static class PeriodicalScan implements Runnable {
        private PeriodicalScan() {
        }

        @Override
        public void run() {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                                                           .getSystemService(Context.WIFI_SERVICE);
            wifiManager.startScan();
            Utils.log("WifiScanHandler started");
        }
    }
}
