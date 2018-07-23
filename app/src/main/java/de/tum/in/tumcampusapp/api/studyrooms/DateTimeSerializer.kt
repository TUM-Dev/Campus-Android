package de.tum.`in`.tumcampusapp.api.studyrooms

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime
import java.lang.reflect.Type

class DateTimeSerializer : JsonDeserializer<DateTime> {

    override fun deserialize(json: JsonElement?,
                             typeOfT: Type?, context: JsonDeserializationContext?): DateTime {
        json?.let {
            return DateTimeUtils.getDateTime(it.asString)
        }

        return DateTime.now()
    }

}