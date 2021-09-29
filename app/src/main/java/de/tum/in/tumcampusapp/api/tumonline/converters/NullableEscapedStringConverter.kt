package de.tum.`in`.tumcampusapp.api.tumonline.converters

import android.os.Build
import android.text.Html
import com.tickaroo.tikxml.TypeConverter

class NullableEscapedStringConverter : TypeConverter<String?> {

    /**
     * Parses leftover html escape sequences
     *
     * @param value String with leftover escape sequences
     * @return unescaped String
     */
    private fun unescapeString(value: String): String {
        // preserve newlines
        val str = value.replace("\n", "<br />")

        return if (Build.VERSION.SDK_INT >= 24) {
            Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(str).toString()
        }
    }

    override fun read(value: String?): String? {
        value?.let {
            return unescapeString(value)
        }
        return null
    }

    override fun write(value: String?): String? {
        return value
    }
}