package de.tum.`in`.tumcampusapp.api.tumonline.interceptors

import okhttp3.CacheControl
import okhttp3.Response
import org.joda.time.Duration
import java.util.concurrent.TimeUnit

class CachingHelper {

    fun isCacheable(url: String) = getCachingDuration(url) != Duration.ZERO

    private fun getCachingDuration(url: String): Duration {
        return cachingDurations
                .filter { url.contains(it.first) }
                .map { it.second }
                .firstOrNull() ?: Duration.ZERO
    }

    fun updateCacheControlHeader(url: String, response: Response): Response {
        val duration = getCachingDuration(url)

        val maxAge = duration.toStandardDays().days
        val cacheControl = CacheControl.Builder()
                .maxAge(maxAge, TimeUnit.DAYS)
                .build()

        return response.newBuilder()
                .removeHeader("Cache-Control")
                .addHeader("Cache-Control", cacheControl.toString())
                .build()
    }

    companion object {

        private val cachingDurations = listOf(
                Pair("kalender", Duration.standardDays(1)),
                Pair("studienbeitragsstatus", Duration.standardDays(1)),
                Pair("veranstaltungenEigene", Duration.standardDays(1)),
                Pair("veranstaltungenDetails", Duration.standardDays(5)),
                Pair("veranstaltungenTermine", Duration.standardDays(5)),
                Pair("personenDetails", Duration.standardDays(5)),
                Pair("noten", Duration.standardDays(1))
        )

    }

}