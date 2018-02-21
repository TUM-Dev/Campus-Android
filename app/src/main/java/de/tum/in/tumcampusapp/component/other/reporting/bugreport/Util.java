package de.tum.in.tumcampusapp.component.other.reporting.bugreport;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import de.tum.in.tumcampusapp.utils.Utils;

public final class Util {

    private static final String NOT_AVAILABLE = "Not available";

    public static String getLog() {
        try {
            Process process = Runtime.getRuntime()
                                     .exec("logcat -d");
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                StringBuilder log = new StringBuilder();
                String line;
                String newLine = System.getProperty(ExceptionHandler.LINE_SEPARATOR);
                while ((line = bufferedReader.readLine()) != null) {
                    log.append(line);
                    log.append(newLine);
                }
                return log.toString();
            }

        } catch (IOException e) { //NOPMD
            //Catch em all, we don't want any trouble here
        }
        return "";
    }

    public static String isGPSOn(Context context) {
        String gpsStatus = "true";

        PackageManager packageManager = context.getPackageManager();
        if (packageManager.checkPermission("android.permission.ACCESS_FINE_LOCATION", G.appPackage) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locManager;
            locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsStatus = "false";
            }
        } else {
            gpsStatus = "not available [permissions]";
        }

        return gpsStatus;
    }

    public static String[] getScreenProperties(Context context) {
        String[] screen = {NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE};

        DisplayMetrics dm = new DisplayMetrics();
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        Utils.logv(Build.VERSION.RELEASE);

        int orientation = display.getRotation();

        screen[0] = Integer.toString(width);
        screen[1] = Integer.toString(height);

        String rotation;
        switch (orientation) {
            case Surface.ROTATION_180:
                rotation = "180";
                break;
            case Surface.ROTATION_270:
                rotation = "270";
                break;
            case Surface.ROTATION_90:
                rotation = "90";
                break;
            case Surface.ROTATION_0:
            default:
                rotation = "normal";
                break;
        }
        screen[2] = rotation;
        screen[3] = Float.toString(dm.xdpi);
        screen[4] = Float.toString(dm.ydpi);

        return screen;
    }

    public static PackageInfo getPackageInfo(Context c) {
        // Get information about the Package
        PackageManager pm = c.getPackageManager();
        try {
            return pm.getPackageInfo(c.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e) {
            Utils.log(e, "Error collecting trace information");
        }
        return null;
    }

    private Util() {
        // Util is a utility class
    }
}
