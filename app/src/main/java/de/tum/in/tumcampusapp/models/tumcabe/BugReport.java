package de.tum.in.tumcampusapp.models.tumcabe;

import android.content.Context;

import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.trace.G;
import de.tum.in.tumcampusapp.trace.Util;

public class BugReport {

    private final String packageName;
    private final String packageVersion;
    private final String packageVersionCode;
    private final String phoneModel;
    private final String androidVersion;
    private final String networkWifi;
    private final String networkMobile;
    private final String gps;
    private final String screenWidth;
    private final String screenHeight;
    private final String screenOrientation;
    private final String screenDpi;
    private final String stacktrace;
    private final String log;

    public BugReport(Context c, String stacktrace, String log) {
        super();
        this.packageName = G.appPackage;
        this.packageVersion = G.appVersion;
        this.packageVersionCode = String.valueOf(G.appVersionCode);

        this.phoneModel = G.phoneModel;
        this.androidVersion = G.androidVersion;

        this.networkWifi = NetUtils.isConnectedWifi(c) ? "true" : "false";
        this.networkMobile = NetUtils.isConnectedMobileData(c) ? "true" : "false";
        this.gps = Util.isGPSOn(c);

        String[] screenProperties = Util.getScreenProperties(c);
        this.screenWidth = screenProperties[0];
        this.screenHeight = screenProperties[1];
        this.screenOrientation = screenProperties[2];
        this.screenDpi = screenProperties[3] + ":" + screenProperties[4];

        this.stacktrace = stacktrace;
        this.log = log;
    }
}
