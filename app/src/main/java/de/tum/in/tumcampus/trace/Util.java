package de.tum.in.tumcampus.trace;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class Util {

    public static String getLog() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            String newLine = System.getProperty("line.separator");
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                log.append(newLine);
            }
            return log.toString();

        } catch (Exception e) {
            //Catch em all, we don't want any trouble here
        }
        return "";
    }


    private static String CheckNetworkConnection(String typeOfConnection) {
        String connected = "false";

        PackageManager packageManager = G.context.getPackageManager();
        if (packageManager.checkPermission("android.permission.ACCESS_NETWORK_STATE", G.appPackage) == PackageManager.PERMISSION_GRANTED) {
            ConnectivityManager cm = (ConnectivityManager) G.context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase(typeOfConnection))
                    if (ni.isConnected())
                        connected = "true";
            }

        } else {
            connected = "not available [permissions]";
        }

        return connected;
    }

    public static String isWifiOn() {

        return CheckNetworkConnection("WIFI");
    }

    public static String isMobileNetworkOn() {

        return CheckNetworkConnection("MOBILE");
    }

    public static String isGPSOn() {
        String gps_status = "true";

        PackageManager packageManager = G.context.getPackageManager();
        if (packageManager.checkPermission("android.permission.ACCESS_FINE_LOCATION", G.appPackage) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locManager;
            locManager = (LocationManager) G.context.getSystemService(Context.LOCATION_SERVICE);
            if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gps_status = "false";
            }
        } else {
            gps_status = "not available [permissions]";
        }

        return gps_status;
    }

    public static String[] ScreenProperties() {
        String screen[] = {"Not available", "Not available", "Not available", "Not available", "Not available"};

        DisplayMetrics dm = new DisplayMetrics();
        Display display = ((WindowManager) G.context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        int width = display.getWidth();
        int height = display.getHeight();

        int orientation;
        Log.i(G.tag, android.os.Build.VERSION.RELEASE);

        //if (android.os.Build.VERSION.RELEASE.startsWith("1.5"))
        orientation = display.getOrientation();
        //else
        //	orientation = display.getRotation();

        screen[0] = Integer.toString(width);
        screen[1] = Integer.toString(height);

        String rotation = "";
        switch (orientation) {
            case Surface.ROTATION_0:
                rotation = "normal";
                break;
            case Surface.ROTATION_180:
                rotation = "180";
                break;
            case Surface.ROTATION_270:
                rotation = "270";
                break;
            case Surface.ROTATION_90:
                rotation = "90";
                break;
        }
        screen[2] = rotation;

        display.getMetrics(dm);
        screen[3] = Float.toString(dm.xdpi);
        screen[4] = Float.toString(dm.ydpi);

        return screen;
    }

    public static PackageInfo getPackageInfo(Context c){
        // Get information about the Package
        PackageManager pm = c.getPackageManager();
        try {
            return pm.getPackageInfo(c.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(G.tag, "Error collecting trace information", e);
        }
        return null;
    }
}
