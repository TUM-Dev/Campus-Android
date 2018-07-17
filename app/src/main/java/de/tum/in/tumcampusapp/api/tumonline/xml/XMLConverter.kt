package de.tum.`in`.tumcampusapp.api.tumonline.xml

import com.tickaroo.tikxml.TikXml
import de.tum.`in`.tumcampusapp.utils.Utils
import okhttp3.ResponseBody
import okio.Okio
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * This class converts [TikXml] objects to their String representation and back.
 */
class XMLConverter {

    companion object {

        private val tikXml = TikXml.Builder()
                .exceptionOnUnreadXml(false)
                .build()

        /**
         * Returns the XML representation of the provided object in String format. Returns null if
         * an [IOException] occurs during conversion.
         *
         * @param body The object to convert into its XML representation
         * @return The XML representation in String format
         */
        @JvmStatic
        fun <T> write(body: T): String? {
            val outputStream = ByteArrayOutputStream()
            val sink = Okio.sink(outputStream)
            val bufferedSink = Okio.buffer(sink)

            return try {
                tikXml.write(bufferedSink, body)
                outputStream.toString(Charsets.UTF_8.name())
            } catch (e: IOException) {
                Utils.log(e)
                return null
            }
        }

        @JvmStatic
        fun responseBody(value: String): ResponseBody = ResponseBody.create(okhttp3.MediaType.parse("text/plain"), value)

        /**
         * Returns an object of the provided class based on the XML representation in the provided
         * String. Returns null if an [IOException] occurs during conversion.
         *
         * @param value The XML in String format
         * @param clazz The desired class of the returned object
         * @return An object of the provided class
         */
        @JvmStatic
        fun <T> read(value: String, clazz: Class<T>): T? {
            val responseBody = ResponseBody.create(okhttp3.MediaType.parse("text/plain"), value)
            val source = responseBody.source()

            return try {
                tikXml.read<T>(source, clazz)
            } catch (e: IOException) {
                Utils.log(e)
                null
            }
        }

    }

}