package de.tum.`in`.tumcampusapp.component.other.departments.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "rowset")
@Deprecated("Not used anywhere. Remove.")
data class OrgItemList(@Element var groups: List<OrgItem> = mutableListOf())