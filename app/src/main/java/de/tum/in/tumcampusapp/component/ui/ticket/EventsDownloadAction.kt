package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl

class EventsDownloadAction(context: Context) :
        (CacheControl) -> Unit {

    private val eventsController = EventsController(context)
    override fun invoke(cacheBehaviour: CacheControl) {
        eventsController.downloadFromService()
    }
}
