package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "TermineLehrveranstaltungen".
 */
@Xml(name = "row")
data class LectureAppointmentsRow(  // TODO: Rename variable
        @PropertyElement var art: String = "",
        @PropertyElement var beginn_datum_zeitpunkt: String = "",
        @PropertyElement var ende_datum_zeitpunkt: String = "",
        @PropertyElement var ort: String = "",
        @PropertyElement var raum_nr: String = "",
        @PropertyElement var raum_nr_architekt: String = "",
        @PropertyElement var termin_betreff: String = "",
        @PropertyElement var lv_grp_nr: String = "",
        @PropertyElement var lv_grp_name: String = ""
)