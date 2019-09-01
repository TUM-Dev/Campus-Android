package de.tum.`in`.tumcampusapp.component.ui.transportation.api

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

interface MvvApiService {

    /*  Documentation for using efa.mvv-muenchen.de
     *
     *  use XML_STOPFINDER_REQUEST to find available stops, e.g.: "Gar"
     *  http://efa.mvv-muenchen.de/mobile/XML_STOPFINDER_REQUEST?outputFormat=JSON&language=de&stateless=1&coordOutputFormat=WGS84&locationServerActive=1&type_sf=any&name_sf=Gar&anyObjFilter_sf=126&reducedAnyPostcodeObjFilter_sf=64&reducedAnyTooManyObjFilter_sf=2&useHouseNumberList=true&anyMaxSizeHitList=500
     *  probably hint the one that gets best="1"
     *  then SORT BY QUALITY, since finding 500+ stops is no fun, probably restrict this to around 10
     *
     *  Breakdown:
     *  http://efa.mvv-muenchen.de/mobile/XML_STOPFINDER_REQUEST? // MVV API base
     *  outputFormat=JSON
     *  &language=de&stateless=1&coordOutputFormat=WGS84&locationServerActive=1 // Common parameters
     *  &type_sf=any // Station type. Just keep "any"
     *  &name_sf=Gar // Search string
     *  &anyObjFilter_sf=126&reducedAnyPostcodeObjFilter_sf=64&reducedAnyTooManyObjFilter_sf=2&useHouseNumberList=true
     *  &anyMaxSizeHitList=500 // How many results there are being provided
     *
     *  use XSLT_DM_REQUEST for departures from stations
     *
     *  Full request: http://efa.mvv-muenchen.de/mobile/XSLT_DM_REQUEST?outputFormat=JSON&language=de&stateless=1&coordOutputFormat=WGS84&type_dm=stop&name_dm=Freising&itOptionsActive=1&ptOptionsActive=1&mergeDep=1&useAllStops=1&mode=direct
     *
     *  Breakdown:
     *  http://efa.mvv-muenchen.de/mobile/XSLT_DM_REQUEST? // MVV API base
     *  outputFormat=JSON // One could also specify XML
     *  &language=de // Tests showed this gets ignored by MVV, but set to language nevertheless
     *  &stateless=1&coordOutputFormat=WGS84 // Common parameters. They are reasonable, so don't change
     *  &type_dm=stop // Station type.
     *  &name_dm=Freising // This is the actual query string
     *  &itOptionsActive=1&ptOptionsActive=1&mergeDep=1&useAllStops=1&mode=direct // No idea what these parameters actually do. Feel free to experiment with them, just don't blame me if anything breaks
     */

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
    fun getDepartures(
        @Query("name_dm") stationId: String,
        @Query("language") language: String = Locale.getDefault().language
    ): Observable<MvvDepartureList>

    /**
     * Find stations by station name prefix
     * @param namePrefix Name prefix
     */
    @GET("XML_STOPFINDER_REQUEST?outputFormat=JSON" +
            "&stateless=1" +
            "&coordOutputFormat=WGS84" +
            "&locationServerActive=1" +
            "&type_sf=stop" +
            "&anyObjFilter_sf=126" +
            "&reducedAnyPostcodeObjFilter_sf=64" +
            "&reducedAnyTooManyObjFilter_sf=2" +
            "&useHouseNumberList=true")
    fun getStations(
        @Query("name_sf") namePrefix: String,
        @Query("anyMaxSizeHitList") maxResults: Int = 10,
        @Query("language") language: String = Locale.getDefault().language
    ): Observable<MvvStationList>
}