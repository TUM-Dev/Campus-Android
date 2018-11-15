package de.tum.`in`.tumcampusapp.api.app

import com.google.gson.*
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.lang.reflect.Type
import java.util.*

/**
 * Serializer for Gson
 *
 * Allows additional date formats to be serialized.
 */
class DateSerializer : JsonDeserializer<DateTime>, JsonSerializer<DateTime> {
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
        dateFormats.forEach {
            try {
                return it.parseDateTime(json?.asString)
            } catch (ignored: Exception) {
            }
        }
        throw JsonParseException("Unparseable date: \"${json?.asString.orEmpty()}\". Supported formats: ${Arrays.toString(formatStrings)}")
    }

    override fun serialize(time: DateTime, typeOfT: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(DateTimeUtils.getDateString(time))
    }
}