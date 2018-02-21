package de.tum.`in`.tumcampusapp.component.other.departments.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * An Element of the Organisation Tree. In the App a List of those Elements is
 * showed ([OrgItemList]). The shown Elements are for Navigation to an
 * Element without child-Element, whose details are then shown.
 */

@Root(name = "row")
data class OrgItem(
        @field:Element(name = "nr")
        var id: String = "",
        @field:Element(name = "name_de")
        var nameDe: String = "",
        @field:Element(name = "name_en")
        var nameEn: String = "",
        @field:Element(name = "parent")
        var parentId: String = "",
        @field:Element(required = false)
        var ebene: String = "",
        @field:Element(required = false)
        var org_gruppe_name: String = "",
        @field:Element(required = false)
        var child_cnt: String = "",
        @field:Element(required = false)
        var sort_hierarchie: String = "",
        @field:Element(required = false)
        var kennung: String = "",
        @field:Element(required = false)
        var org_typ_name: String = "")
