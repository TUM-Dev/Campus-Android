package de.tum.in.tumcampus.services;

import android.content.Intent;
import android.widget.RemoteViewsService;

import de.tum.in.tumcampus.widgets.RemoteViewFactories.MensaRemoteViewFactory;

/**
 * Created by a2k on 7/6/2015.
 */
public class MensaWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MensaRemoteViewFactory(this.getApplicationContext(), intent);
    }

}

