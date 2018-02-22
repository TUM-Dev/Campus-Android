package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "TermineLehrveranstaltungen".
 *
 * @see [SimpleXML tutorial](http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php)
 */
@Root(name = "row")
data class LectureAppointmentsRow(@field:Element(required = false)
                                  var art: String = "",
                                  @field:Element
                                  var beginn_datum_zeitpunkt: String = "",
                                  @field:Element
                                  var ende_datum_zeitpunkt: String = "",
                                  @field:Element(required = false)
                                  var ort: String = "",
                                  @field:Element(required = false)
                                  var raum_nr: String = "",
                                  @field:Element(required = false)
                                  var raum_nr_architekt: String = "",
                                  @field:Element(required = false)
                                  var termin_betreff: String = "",
                                  @field:Element(required = false)
                                  var lv_grp_nr: String = "",
                                  @field:Element(required = false)
                                  var lv_grp_name: String = "")