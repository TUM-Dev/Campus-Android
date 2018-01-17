package de.tum.`in`.tumcampusapp.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import de.tum.`in`.tumcampusapp.auxiliary.Const.DATE_AND_TIME
import de.tum.`in`.tumcampusapp.auxiliary.Const.DATE_ONLY
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

/**
 * Serializer for Gson
 *
 * Allows additional dateformats to be serialized.
 *
 */
class DateSerializer : JsonDeserializer<Date> {

    private val dateFormats = arrayOf(DATE_AND_TIME, DATE_ONLY)

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
        dateFormats.forEach {
            try {
                return SimpleDateFormat(it, Locale.GERMAN).parse(json?.asString)
            } catch (throwable: JsonParseException){}
        }
        throw JsonParseException("Unparseable date: \"" + (json?.asString ?: "")
                + "\". Supported formats: " + Arrays.toString(dateFormats))
    }

}