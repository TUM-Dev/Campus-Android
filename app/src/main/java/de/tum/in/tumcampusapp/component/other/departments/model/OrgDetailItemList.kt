package de.tum.`in`.tumcampusapp.component.other.departments.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the de-serialization of the output of TUMOnline to the method "sucheLehrveranstaltungen".
 */

@Xml(name = "rowset")
data class OrgDetailItemList(@Element var groups: List<OrgDetailsItem> = mutableListOf())
