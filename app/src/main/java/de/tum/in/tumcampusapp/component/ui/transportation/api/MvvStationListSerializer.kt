package de.tum.`in`.tumcampusapp.component.ui.transportation.api

import com.google.gson.*
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult
import java.lang.reflect.Type

/**
 * Parses the weird MVV XML_STOPFINDER_REQUEST response
 */
class MvvStationListSerializer : JsonDeserializer<MvvStationList> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): MvvStationList {
        if (json !is JsonObject) {
            throw JsonParseException("Invalid MvvStationList: $json")
        }

        val points = json.getAsJsonObject("stopFinder").get("points")

        // This is where the fun starts: points can either be an Object, an Array or null
        // empty result
        if (points is JsonNull) {
            return MvvStationList(emptyList())
        }

        // singleton result, i.e. exact match
        if (points is JsonObject) {
            return MvvStationList(listOf(StationResult.fromJson(points.get("point") as JsonObject)))
        }

        if (points is JsonArray) {
            val resultList = points.map {
                StationResult.fromJson(it as JsonObject)
            }
            return MvvStationList(resultList)
        }

        throw JsonParseException("Unknown MvvStationList: $json")
    }
}