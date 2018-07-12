package de.tum.`in`.tumcampusapp.component.ui.transportation.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MvvApiService {
// TODO: check if language parameter makes any difference

    /**
     * Get all departures for a station.
     * @param stationId Station ID, station name might or might not work
     */
    @GET("XSLT_DM_REQUEST?outputFormat=JSON" +
            "&stateless=1" +
            "&coordOutputFormat=WGS84" +
            "&type_dm=stop" +
            "&itOptionsActive=1" +
            "&ptOptionsActive=1" +
            "&mergeDep=1" +
            "&useAllStops=1" +
            "&mode=direct")
    fun getDepartures(@Query("name_dm") stationId: String): Call<MvvDepartureList>

    /**
     * Find stations by station name prefix
     * TODO: Check if escaping query parameters is necessary
     * @param namePrefix Name prefix
     */
    @GET("XML_STOPFINDER_REQUEST?outputFormat=JSON" +
            "&stateless=1" +
            "&coordOutputFormat=WGS84" +
            "&type_dm=stop" +
            "&itOptionsActive=1" +
            "&ptOptionsActive=1" +
            "&mergeDep=1" +
            "&useAllStops=1" +
            "&mode=direct")
    fun getStations(@Query("name_sf") namePrefix: String): Call<List<String>>
}