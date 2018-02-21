package de.tum.in.tumcampusapp.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.RemoteViewsService;

import de.tum.in.tumcampusapp.component.ui.cafeteria.widget.MensaRemoteViewFactory;

@SuppressLint("Registered")
public class MensaWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MensaRemoteViewFactory(this.getApplicationContext(), intent);
    }

}

