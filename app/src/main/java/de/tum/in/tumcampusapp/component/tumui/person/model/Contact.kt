package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.Attribute
import java.io.Serializable

/**
 * Contact information of a TUM [Employee] or a generic [Person].
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
data class Contact(
        @Attribute(name = "zusatz_info")
        var additionalInfo: String = "",
        @Attribute(name = "fax")
        var fax: String = "",
        @Attribute(name = "www_homepage")
        var homepage: String = "",
        @Attribute(name = "mobiltelefon")
        var mobilephone: String = "",
        @Attribute(name = "telefon")
        var telefon: String = "") :
        Serializable {

    companion object {
        private const val serialVersionUID = 4413581972047241018L
    }
}
