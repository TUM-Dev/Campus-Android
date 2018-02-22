package de.tum.`in`.tumcampusapp.component.tumui.person.model

import org.simpleframework.xml.Element

import java.io.Serializable

/**
 * Contact information of a TUM [Employee] or a generic [Person].
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
data class Contact(
        @field:Element(name = "zusatz_info", required = false)
        var additionalInfo: String = "",
        @field:Element(name = "fax", required = false)
        var fax: String = "",
        @field:Element(name = "www_homepage", required = false)
        var homepage: String = "",
        @field:Element(name = "mobiltelefon", required = false)
        var mobilephone: String = "",
        @field:Element(name = "telefon", required = false)
        var telefon: String = "") :
        Serializable {

    companion object {
        private const val serialVersionUID = 4413581972047241018L
    }
}
