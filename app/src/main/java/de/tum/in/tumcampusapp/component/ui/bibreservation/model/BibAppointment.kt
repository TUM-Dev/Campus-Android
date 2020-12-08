package de.tum.`in`.tumcampusapp.component.ui.bibreservation.model

import com.google.gson.annotations.SerializedName
import org.joda.time.format.DateTimeFormat
import java.util.*

data class BibAppointment(
    @SerializedName("bib")
    val bib: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("reservationKey")
    val reservationKey: String) {

    fun getFormattedDate(): String {
        // might need to be updated when new languages are added
        if (Locale.getDefault() == Locale.GERMAN || Locale.getDefault() == Locale.GERMANY)
            return dateFormatGerman.print(parseFormat.parseDateTime(date))
        return dateFormatEnglish.print(parseFormat.parseDateTime(date))
    }

    companion object {
        val parseFormat = DateTimeFormat.forPattern("EEEE, dd. MMMM yyyy").withLocale(Locale.GERMAN)
        val dateFormatEnglish = DateTimeFormat.forPattern("EEEE MMM. dd yyyy").withLocale(Locale.US)
        val dateFormatGerman = DateTimeFormat.forPattern("EEEE dd. MMM yyyy").withLocale(Locale.GERMAN)
    }

}
