package de.tum.`in`.tumcampusapp.component.ui.updatenote

import android.content.Context
import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.IOException

class UpdateNoteController(context: Context) {
    val mContext = context

    fun downloadUpdateNote() {
        try {
            val note = TUMCabeClient.getInstance(mContext)
                    .getUpdateNote(BuildConfig.VERSION_CODE)
            Utils.setSetting(mContext, Const.UPDATE_MESSAGE, note?.updateNote ?: "")
        } catch (e: IOException) {
            Utils.log(e)
        }
    }
}