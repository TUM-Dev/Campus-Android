package de.tum.`in`.tumcampusapp.service

import android.content.Intent
import android.widget.RemoteViewsService
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.widget.MensaRemoteViewFactory

class MensaWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return MensaRemoteViewFactory(this.applicationContext)
    }

}

