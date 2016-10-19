package de.tum.in.tumcampusapp.models.tumcabe;

import android.content.Context;

import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.trace.G;
import de.tum.in.tumcampusapp.trace.Util;

public class BugReport {

    private String packageName;
    private String packageVersion;
    private String packageVersionCode;
    private String phoneModel;
    private String androidVersion;
    private String networkWifi;
    private String networkMobile;
    private String gps;
    private String screenWidth;
    private String screenHeight;
    private String screenOrientation;
    private String screenDpi;
    private String stacktrace;
    private String log;


    public BugReport(Context c, String stacktrace, String log) {
        super();
        this.packageName = G.appPackage;
        this.packageVersion = G.appVersion;
        this.packageVersionCode = "" + G.appVersionCode;

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
