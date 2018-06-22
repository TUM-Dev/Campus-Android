package de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml
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
@Xml(name = "row")
data class Tuition(@Attribute(name = "frist")  // TODO: Rename variables
                   var frist: String = "",
                   @Attribute(name = "semester_bezeichnung")
                   var semesterBez: String = "",
                   @Attribute(name = "soll")
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

}
