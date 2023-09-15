package de.tum.`in`.tumcampusapp.component.ui.updatenote

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.BackendClient
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import javax.inject.Inject

class UpdateNoteDownloadAction @Inject constructor(
    private val mContext: Context
) : DownloadWorker.Action {
    override fun execute(cacheBehaviour: CacheControl) {
        val savedNote = Utils.getSetting(mContext, Const.UPDATE_MESSAGE, "")
        if (savedNote.isNotEmpty()) {
            // note has already been downloaded
            return
        }

        BackendClient.getInstance().getUpdateNote(
            { note -> Utils.setSetting(mContext, Const.UPDATE_MESSAGE, note.updateNote) },
            {
                if (it.status.code == io.grpc.Status.NOT_FOUND.code) {
                    return@getUpdateNote
                }
                Utils.log(it)
            }
        )
    }
}
