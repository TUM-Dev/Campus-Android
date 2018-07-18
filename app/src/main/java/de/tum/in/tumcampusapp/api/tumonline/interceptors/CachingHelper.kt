package de.tum.`in`.tumcampusapp.api.tumonline.interceptors

import okhttp3.CacheControl
import okhttp3.Response
import org.joda.time.Duration
import java.util.concurrent.TimeUnit

class CachingHelper {

    fun isCacheable(url: String) = cachingDurations.keys.any { url.contains(it) }

    fun updateCacheControlHeader(url: String, response: Response): Response {
        val duration = cachingDurations
                .entries
                .filter { url.contains(it.key) }
                .map { it.value }
                .firstOrNull()

        return if (duration != null) {
            val maxAge = duration.toStandardDays().days
            val cacheControl = CacheControl.Builder()
                    .maxAge(maxAge, TimeUnit.DAYS)
                    .build()
            response.newBuilder()
                    .removeHeader("Cache-Control")
                    .addHeader("Cache-Control", cacheControl.toString())
                    .build()
        } else {
            response
        }
    }

    companion object {

        private val cachingDurations = mapOf(
                "kalender" to Duration.standardDays(1),
                "studienbeitragsstatus" to Duration.standardDays(1),
                "veranstaltungenEigene" to Duration.standardDays(1),
                "veranstaltungenDetails" to Duration.standardDays(5),
                "veranstaltungenTermine" to Duration.standardDays(5),
                "personenDetails" to Duration.standardDays(5),
                "noten" to Duration.standardDays(1)
        )

    }

}