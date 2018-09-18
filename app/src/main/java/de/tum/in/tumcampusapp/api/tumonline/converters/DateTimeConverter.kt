package de.tum.`in`.tumcampusapp.api.tumonline.converters

import com.tickaroo.tikxml.TypeConverter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class DateTimeConverter : TypeConverter<DateTime?> {

    private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")

    override fun read(value: String?): DateTime? {
        value?.let {
            return formatter.parseDateTime(it)
        }
        return null
    }

    override fun write(value: DateTime?): String {
        value?.let {
            return formatter.print(it)
        }
        return ""
    }

}
