package de.tum.`in`.tumcampusapp.component.other.departments.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

/**
 * Model for the organisation details
 * Information about the organisation
 */
@Xml(name = "row")
data class OrgDetailsItem(
        @PropertyElement(name = "nr")
        var id: String = "",
        @PropertyElement(name = "name")
        var name: String = "",
        @PropertyElement(name = "zusatz_info_name")
        var additionalInfoCaption: String = "",
        @PropertyElement(name = "zusatz_info")
        var additionalInfoText: String = "",
        @PropertyElement(name = "kennung")
        var code: String = "", // Organisation Code, e.g. "TUZESSB"
        @PropertyElement(name = "email_adresse")
        var contactEmail: String = "",
        @PropertyElement(name = "fax_nummer")
        var contactFax: String = "", // Fax Number
        @PropertyElement(name = "sekretariat_info")
        var contactLocality: String = "", // Town, to which the organisation belongs to
        @PropertyElement(name = "www_homepage")
        var contactLocationURL: String = "", // URL to a Google Maps Site containing the
        @PropertyElement(name = "ansprechpartner")
        var contactName: String = "",
        @PropertyElement(name = "adresse_text")
        var contactStreet: String = "", // Street and Street number of the Organisation
        @PropertyElement(name = "telefon_nummer")
        var contactTelephone: String = "", // Phone Number, usually to the secretary of the organisation
        @PropertyElement(name = "bibliothek_info")
        var contactAdditionalInfo: String = "")
