package de.tum.`in`.tumcampusapp.component.ui.updatenote.model

import de.tum.`in`.tumcampusapp.api.backend.GetUpdateNoteReply

data class UpdateNote(var updateNote: String) {
    companion object {
        fun fromProto(it: GetUpdateNoteReply?): UpdateNote {
            return UpdateNote(
                updateNote = it?.message ?: ""
            )
        }
    }
}
