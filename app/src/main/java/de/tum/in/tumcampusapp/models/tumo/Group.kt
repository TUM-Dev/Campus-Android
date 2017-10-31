package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

import java.io.Serializable

/**
 * Group that describes some organizational entity a person/employee can belong
 * to. Note: This model is based on the TUMOnline web service response format
 * for a corresponding request.
 */

@Root(name = "gruppe", strict = false)
data class Group(@field:Element(name = "beschreibung", required = false)
                 var description: String = "",
                 @field:Element(name = "kennung")
                 var id: String = "",
                 @field:Element(name = "org")
                 var org: String = "",
                 @field:Element(name = "titel", required = false)
                 var title: String = "") :
        Serializable {
    companion object {
        private const val serialVersionUID = -3430225489964912473L
    }
}