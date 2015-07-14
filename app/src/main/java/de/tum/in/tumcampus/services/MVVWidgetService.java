package de.tum.in.tumcampus.services;

import android.content.Intent;
import android.widget.RemoteViewsService;

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
        if (remoteViewFactory != null) {
            remoteViewFactory.callRecentVisitedStation();
        } else {
            isDone = false;
        }
        return isDone;

    }

}

