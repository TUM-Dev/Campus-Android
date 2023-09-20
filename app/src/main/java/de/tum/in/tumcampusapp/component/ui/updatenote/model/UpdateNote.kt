package de.tum.`in`.tumcampusapp.component.ui.updatenote.model

import app.tum.campus.api.GetUpdateNoteReply

data class UpdateNote(var updateNote: String) {
    companion object {
        fun fromProto(it: GetUpdateNoteReply?): UpdateNote {
            return UpdateNote(
                updateNote = it?.message ?: ""
            )
        }
    }
}
