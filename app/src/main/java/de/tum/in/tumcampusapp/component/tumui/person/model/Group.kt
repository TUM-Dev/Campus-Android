package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import java.io.Serializable

/**
 * Group that describes some organizational entity a person/employee can belong
 * to. Note: This model is based on the TUMOnline web service response format
 * for a corresponding request.
 */

@Xml(name = "gruppe")
data class Group(
    @PropertyElement(name = "beschreibung")
    var description: String = "",
    @PropertyElement(name = "kennung")
    var id: String = "",
    @PropertyElement(name = "org")
    var org: String = "",
    @PropertyElement(name = "titel")
    var title: String = ""
) :
        Serializable {
    companion object {
        private const val serialVersionUID = -3430225489964912473L
    }
}