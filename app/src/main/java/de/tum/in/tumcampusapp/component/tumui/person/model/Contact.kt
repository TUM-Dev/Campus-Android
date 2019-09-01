package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import java.io.Serializable

/**
 * Contact information of a TUM [Employee] or a generic [Person].
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Xml
data class Contact(
    @PropertyElement(name = "zusatz_info")
    var additionalInfo: String = "",
    @PropertyElement(name = "fax")
    var fax: String = "",
    @PropertyElement(name = "www_homepage")
    var homepage: String = "",
    @PropertyElement(name = "mobiltelefon")
    var mobilephone: String = "",
    @PropertyElement(name = "telefon")
    var telefon: String = ""
) : Serializable {

    companion object {
        private const val serialVersionUID = 4413581972047241018L
    }
}
