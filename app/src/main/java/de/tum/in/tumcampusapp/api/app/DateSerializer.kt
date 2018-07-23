package de.tum.`in`.tumcampusapp.api.app

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.lang.reflect.Type
import java.util.*

/**
 * Serializer for Gson
 *
 * Allows additional date formats to be serialized.
 */
class DateSerializer : JsonDeserializer<DateTime> {
    private val formatStrings = arrayOf(
            "yyyy-MM-dd",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )

    private val dateFormats = formatStrings.map {
        DateTimeFormat.forPattern(it).withLocale(Locale.GERMAN)
    }

    override fun deserialize(json: JsonElement?,
                             typeOfT: Type?, context: JsonDeserializationContext?): DateTime {
        val value = json?.asString ?: return DateTime.now()

        dateFormats.forEach {
            try {
                return it.parseDateTime(value)
            } catch (ignored: Exception) {
            }
        }

        throw JsonParseException("Unparseable date: \"${value}\". Supported formats: ${Arrays.toString(formatStrings)}")
    }
}