package de.tum.`in`.tumcampusapp.component.ui.transportation.api

import com.google.gson.*
import org.joda.time.DateTime
import java.lang.reflect.Type

/**
 * Parses the MVV specific dateTime, with json elements of:
 * year, month, day, hour, minute
 */
class MvvDateSerializer : JsonDeserializer<DateTime> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DateTime {
        if (json !is JsonObject) {
            throw JsonParseException("Invalid MvvDateTime: $json")
        }
        val year = json.get("year")?.asInt.orThrow(json)
        val month = json.get("month")?.asInt.orThrow(json)
        val day = json.get("day")?.asInt.orThrow(json)
        val hour = json.get("hour")?.asInt.orThrow(json)
        val minute = json.get("minute")?.asInt.orThrow(json)

        return DateTime()
                .withDate(year, month, day)
                .withTime(hour, minute, 0, 0)
    }

    private fun <T> T?.orThrow(json: JsonElement): T {
        return this ?: throw JsonParseException("Invalid MvvDateTime: $json")
    }
}