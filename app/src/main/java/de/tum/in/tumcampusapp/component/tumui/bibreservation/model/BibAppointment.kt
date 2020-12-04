package de.tum.`in`.tumcampusapp.component.tumui.bibreservation.model

import com.google.gson.annotations.SerializedName
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

data class BibAppointment(
    @SerializedName("bib")
    val bib: String,
    @SerializedName("from")
    val from: String,
    @SerializedName("til")
    val til: String,
    @SerializedName("reservationId")
    val reservationId: String) {

    override fun toString(): String {
        return "$bib on $from id:$reservationId"
    }

    fun getFromTime(): String {
        return fmt.parseDateTime(from).toString(fmtTime)
    }

    fun getTilTime(): String {
        return fmt.parseDateTime(til).toString(fmtTime)
    }

    fun getDay(): String {
        return fmt.parseDateTime(from).toString(fmtDate)
    }

    companion object {
        val fmt: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val fmtTime: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
        val fmtDate: DateTimeFormatter = DateTimeFormat.forPattern("dd.MM.YYYY")
    }

}