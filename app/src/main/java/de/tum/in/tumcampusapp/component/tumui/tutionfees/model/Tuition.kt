package de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model

import android.content.Context
import android.content.Intent
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.converters.DateTimeConverter
import de.tum.`in`.tumcampusapp.api.tumonline.converters.FloatConverter
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeesActivity
import org.joda.time.DateTime
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
data class Tuition(@PropertyElement(name = "frist", converter = DateTimeConverter::class)
                   val deadline: DateTime,
                   @PropertyElement(name = "semester_bezeichnung")
                   val semester: String,
                   @PropertyElement(name = "soll", converter = FloatConverter::class)
                   val amount: Float) {

    fun getIntent(context: Context): Intent = Intent(context, TuitionFeesActivity::class.java)

    val isPaid: Boolean
        get() = amount == 0f

    fun getAmountText(context: Context): String {
        return try {
            val amountText = String.format(Locale.getDefault(), "%.2f", amount)
            return "$amountText €"
        } catch (e: ParseException) {
            context.getString(R.string.not_available)
        }
    }

}
