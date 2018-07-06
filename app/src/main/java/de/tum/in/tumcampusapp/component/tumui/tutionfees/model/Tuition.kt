package de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime

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
data class Tuition(@PropertyElement(name = "frist")
                   val deadline: String,
                   @PropertyElement(name = "semester_bezeichnung")
                   val semester: String,
                   @PropertyElement(name = "soll")
                   val amount: String) {

    // TODO: Add TypeConverter once fully switched to JodaTime

    val outstandingBalance: Float
        get() {
            return try {
                NumberFormat.getInstance(Locale.GERMAN)
                        .parse(amount)
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
                amount
            }
        }

    val dueDate: DateTime
        get() = DateTimeUtils.getDate(deadline)
}
