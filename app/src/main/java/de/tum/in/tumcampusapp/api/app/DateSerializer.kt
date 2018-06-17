package de.tum.`in`.tumcampusapp.api.app

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import de.tum.`in`.tumcampusapp.utils.Const.DATE_AND_TIME
import de.tum.`in`.tumcampusapp.utils.Const.DATE_ONLY
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.lang.reflect.Type
import java.util.*

/**
 * Serializer for Gson
 *
 * Allows additional dateformats to be serialized.
 */
class DateSerializer : JsonDeserializer<DateTime> {
    private val formatStrings = arrayOf(
            DATE_AND_TIME,
            DATE_ONLY
    )
    private val dateFormats = formatStrings.map {
        DateTimeFormat.forPattern(it).withLocale(Locale.GERMAN)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DateTime {
        dateFormats.forEach {
            try {
                return it.parseDateTime(json?.asString)
            } catch (ignored: Exception) {
            }
        }
        throw JsonParseException("Unparseable date: \"${json?.asString.orEmpty()}\". Supported formats: ${Arrays.toString(formatStrings)}")
    }
}