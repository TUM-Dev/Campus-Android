package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "TermineLehrveranstaltungen".
 */
@Xml(name = "row")
data class LectureAppointmentsRow(  // TODO: Rename variable
        @Attribute
        var art: String = "",
        @Attribute
        var beginn_datum_zeitpunkt: String = "",
        @Attribute
        var ende_datum_zeitpunkt: String = "",
        @Attribute
        var ort: String = "",
        @Attribute
        var raum_nr: String = "",
        @Attribute
        var raum_nr_architekt: String = "",
        @Attribute
        var termin_betreff: String = "",
        @Attribute
        var lv_grp_nr: String = "",
        @Attribute
        var lv_grp_name: String = ""
)