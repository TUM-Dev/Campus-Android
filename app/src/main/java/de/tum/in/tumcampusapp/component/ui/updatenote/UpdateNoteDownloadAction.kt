package de.tum.`in`.tumcampusapp.component.ui.updatenote

import android.content.Context
import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.IOException
import javax.inject.Inject

class UpdateNoteDownloadAction @Inject constructor(
    val mContext: Context
) : DownloadWorker.Action {
    override fun execute(cacheBehaviour: CacheControl) {
        val savedNote = Utils.getSetting(mContext, Const.UPDATE_MESSAGE, "")
        if (savedNote.isNotEmpty()) {
            // note has already been downloaded
            return
        }

        try {
            val note = TUMCabeClient.getInstance(mContext).getUpdateNote(BuildConfig.VERSION_CODE)
            Utils.setSetting(mContext, Const.UPDATE_MESSAGE, note?.updateNote ?: "")
        } catch (e: IOException) {
            Utils.log(e)
        }
    }
}