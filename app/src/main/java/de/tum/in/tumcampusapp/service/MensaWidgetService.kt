package de.tum.`in`.tumcampusapp.service

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.RemoteViewsService
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.widget.MensaRemoteViewFactory

@SuppressLint("Registered")
class MensaWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        return MensaRemoteViewFactory(this.applicationContext)
    }

}

