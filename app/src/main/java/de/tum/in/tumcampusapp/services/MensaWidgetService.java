package de.tum.in.tumcampusapp.services;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

import de.tum.in.tumcampusapp.widgets.RemoteViewFactories.MensaRemoteViewFactory;

@SuppressLint("Registered")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MensaWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MensaRemoteViewFactory(this.getApplicationContext(), intent);
    }

}

