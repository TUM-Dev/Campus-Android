package de.tum.`in`.tumcampusapp.component.other.departments.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

/**
 * An Element of the Organisation Tree. In the App a List of those Elements is
 * showed ([OrgItemList]). The shown Elements are for Navigation to an
 * Element without child-Element, whose details are then shown.
 */
@Xml(name = "row")
data class OrgItem(
        @Attribute(name = "nr")
        var id: String = "",
        @Attribute(name = "name_de")
        var nameDe: String = "",
        @Attribute(name = "name_en")
        var nameEn: String = "",
        @Attribute(name = "parent")
        var parentId: String = "",
        @Attribute
        var ebene: String = "",
        @Attribute
        var org_gruppe_name: String = "",
        @Attribute
        var child_cnt: String = "",
        @Attribute
        var sort_hierarchie: String = "",
        @Attribute
        var kennung: String = "",
        @Attribute
        var org_typ_name: String = "")
