package de.tum.`in`.tumcampusapp.component.other.departments.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

/**
 * Model for the organisation details
 * Information about the organisation
 */
@Xml(name = "row")
data class OrgDetailsItem(
        @Attribute(name = "nr")
        var id: String = "",
        @Attribute(name = "name")
        var name: String = "",
        @Attribute(name = "zusatz_info_name")
        var additionalInfoCaption: String = "",
        @Attribute(name = "zusatz_info")
        var additionalInfoText: String = "",
        @Attribute(name = "kennung")
        var code: String = "", // Organisation Code, e.g. "TUZESSB"
        @Attribute(name = "email_adresse")
        var contactEmail: String = "",
        @Attribute(name = "fax_nummer")
        var contactFax: String = "", // Fax Number
        @Attribute(name = "sekretariat_info")
        var contactLocality: String = "", // Town, to which the organisation belongs to
        @Attribute(name = "www_homepage")
        var contactLocationURL: String = "", // URL to a Google Maps Site containing the
        @Attribute(name = "ansprechpartner")
        var contactName: String = "",
        @Attribute(name = "adresse_text")
        var contactStreet: String = "", // Street and Street number of the Organisation
        @Attribute(name = "telefon_nummer")
        var contactTelephone: String = "", // Phone Number, usually to the secretary of the organisation
        @Attribute(name = "bibliothek_info")
        private var contactAdditionalInfo: String = "")
