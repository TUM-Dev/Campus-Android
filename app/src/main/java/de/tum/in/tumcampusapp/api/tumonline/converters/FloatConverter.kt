package de.tum.`in`.tumcampusapp.api.tumonline.converters

import com.tickaroo.tikxml.TypeConverter
import java.text.NumberFormat
import java.text.ParseException
import java.util.*

class FloatConverter : TypeConverter<Float> {

    override fun read(value: String?): Float {
        return try {
            NumberFormat.getInstance(Locale.GERMAN)
                    .parse(value)
                    .toFloat()
        } catch (e: ParseException) {
            0f
        }
    }

    override fun write(value: Float?) = value?.toString() ?: ""
}