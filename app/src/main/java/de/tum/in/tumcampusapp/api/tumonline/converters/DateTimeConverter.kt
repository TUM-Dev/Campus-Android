package de.tum.`in`.tumcampusapp.api.tumonline.converters

import com.google.gson.JsonParseException
import com.tickaroo.tikxml.TypeConverter
import de.tum.`in`.tumcampusapp.utils.tryOrNull
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class DateTimeConverter : TypeConverter<DateTime?> {

    private val formats = arrayOf(
            "yyyy-MM-dd",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd HH:mm:ss"
    )

    override fun read(value: String?): DateTime? {
        if (value.isNullOrEmpty()) {
            return null
        }
        return parseString(value)
    }

    private fun parseString(value: String): DateTime? {
        formats.forEach { format ->
            val dateTime = tryOrNull { DateTimeFormat.forPattern(format).parseDateTime(value) }
            dateTime?.let {
                return it
            }
        }

        throw JsonParseException("Invalid date format: $value")
    }

    override fun write(value: DateTime?): String {
        value?.let {
            return formatDateTime(it)
        }
        return ""
    }

    private fun formatDateTime(dateTime: DateTime): String {
        formats.forEach { format ->
            val dateString = tryOrNull { DateTimeFormat.forPattern(format).print(dateTime) }
            dateString?.let {
                return it
            }
        }

        throw JsonParseException("Invalid date format: $dateTime")
    }
}
