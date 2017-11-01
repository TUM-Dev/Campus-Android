package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * Model for the organisation details
 * Information about the organisation
 */
@Root(name = "row")
data class OrgDetailsItem(
        @field:Element(name = "nr")
        var id: String = "",
        @field:Element(name = "name")
        var name: String = "",
        @field:Element(name = "zusatz_info_name", required = false)
        var additionalInfoCaption: String = "",
        @field:Element(name = "zusatz_info", required = false)
        var additionalInfoText: String = "",
        @field:Element(name = "kennung")
        var code: String = "", // Organisation Code, e.g. "TUZESSB"
        @field:Element(name = "email_adresse", required = false)
        var contactEmail: String = "",
        @field:Element(name = "fax_nummer", required = false)
        var contactFax: String = "", // Fax Number
        @field:Element(name = "sekretariat_info", required = false)
        var contactLocality: String = "", // Town, to which the organisation belongs to
        @field:Element(name = "www_homepage", required = false)
        var contactLocationURL: String = "", // URL to a Google Maps Site containing the
        @field:Element(name = "ansprechpartner", required = false)
        var contactName: String = "",
        @field:Element(name = "adresse_text", required = false)
        var contactStreet: String = "", // Street and Street number of the Organisation
        @field:Element(name = "telefon_nummer", required = false)
        var contactTelephone: String = "", // Phone Number, usually to the secretary of the organisation
        @field:Element(name = "bibliothek_info", required = false)
        private var contactAdditionalInfo: String = "")
