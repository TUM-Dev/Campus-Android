package de.tum.`in`.tumcampusapp.api.tumonline.converters

import com.tickaroo.tikxml.TypeConverter
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime

class DateTimeConverter : TypeConverter<DateTime?> {

    override fun read(value: String?): DateTime? {
        value?.let {
            return DateTimeUtils.getDateTime(value)
        }

        return null
    }

    override fun write(value: DateTime?): String {
        value?.let {
            return DateTimeUtils.getDateTimeString(value)
        }
        return ""
    }

}