package de.tum.`in`.tumcampusapp.component.ui.alarm.model

import java.io.Serializable

/**
 * Used for parsing the GCM json payload for the 'alert' type
 */
data class GCMAlert(var silent: Boolean = true) : Serializable {
    companion object {
        private const val serialVersionUID = 3906290674499996501L
    }
}
