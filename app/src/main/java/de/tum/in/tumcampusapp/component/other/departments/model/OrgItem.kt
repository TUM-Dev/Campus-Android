package de.tum.`in`.tumcampusapp.component.other.departments.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

/**
 * An Element of the Organisation Tree. In the App a List of those Elements is
 * showed ([OrgItemList]). The shown Elements are for Navigation to an
 * Element without child-Element, whose details are then shown.
 */
@Xml(name = "row")
@Deprecated("Not used anywhere. Remove.")
data class OrgItem(
        @PropertyElement(name = "nr")
        var id: String = "",
        @PropertyElement(name = "name_de")
        var nameDe: String = "",
        @PropertyElement(name = "name_en")
        var nameEn: String = "",
        @PropertyElement(name = "parent")
        var parentId: String = "",
        @PropertyElement
        var ebene: String = "",
        @PropertyElement
        var org_gruppe_name: String = "",
        @PropertyElement
        var child_cnt: String = "",
        @PropertyElement
        var sort_hierarchie: String = "",
        @PropertyElement
        var kennung: String = "",
        @PropertyElement
        var org_typ_name: String = "")
