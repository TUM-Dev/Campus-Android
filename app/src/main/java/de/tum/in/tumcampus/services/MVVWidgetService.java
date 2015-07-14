package de.tum.in.tumcampus.services;

import android.content.Intent;
import android.widget.RemoteViewsService;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.widgets.RemoteViewFactories.MVVRemoteViewFactory;


public class MVVWidgetService extends RemoteViewsService {
    public static MVVRemoteViewFactory remoteViewFactory = null;
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        remoteViewFactory = new MVVRemoteViewFactory(this.getApplicationContext(), intent);
        return remoteViewFactory;
    }
    public static boolean loadRecentData() {
        boolean isDone = true;
        Utils.log("WidgetMVV loadRecent Data");
        if (remoteViewFactory != null) {
            remoteViewFactory.callRecentVisitedStation();
            Utils.log("WidgetMVV loadRecent Data SUCCESS");

        } else {
            isDone = false;
            Utils.log("WidgetMVV loadRecent Data FAILURE");
        }
        return isDone;

    }

}

