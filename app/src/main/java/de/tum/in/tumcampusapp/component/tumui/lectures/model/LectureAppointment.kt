package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "TermineLehrveranstaltungen".
 */
@Xml(name = "row")
data class LectureAppointment(
        @PropertyElement(name = "art") var type: String? = null,
        @PropertyElement(name = "beginn_datum_zeitpunkt") var startTime: String = "",  // TODO: TypeConverters
        @PropertyElement(name = "ende_datum_zeitpunkt") var endTime: String = "",
        @PropertyElement(name = "ort") var location: String? = null,
        @PropertyElement(name = "raum_nr") var roomNumber: String? = null,
        @PropertyElement(name = "raum_nr_architekt") var roomNumberArchitect: String? = null,
        @PropertyElement(name = "termin_betreff") var title: String = "",
        @PropertyElement(name = "lv_grp_nr") var lectureGroupId: String? = null,
        @PropertyElement(name = "lv_grp_name") var lectureGroupName: String? = null
)