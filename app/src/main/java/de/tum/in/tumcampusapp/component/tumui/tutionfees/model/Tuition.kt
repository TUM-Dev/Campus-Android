package de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model

import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import java.text.NumberFormat
import java.text.ParseException
import java.util.*

/**
 * Class holding tuition information.
 *
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "row", strict = false)
data class Tuition(@field:Element(name = "frist")
                   var frist: String = "",
                   @field:Element(name = "semester_bezeichnung")
                   var semesterBez: String = "",
                   @field:Element(name = "soll")
                   var soll: String = "") {

    val outstandingBalance: Float
        get() {
            return try {
                NumberFormat.getInstance(Locale.GERMAN)
                        .parse(soll)
                        .toFloat()
            } catch (e: ParseException) {
                0f
            }
        }

    val outstandingBalanceText: String
        get() {
            return try {
                val amountText = String.format(Locale.getDefault(), "%.2f", outstandingBalance)
                return "$amountText €"
            } catch (e: ParseException) {
                soll
            }
        }

    val dueDate: DateTime
        get() = DateTimeUtils.getDate(frist)
}
