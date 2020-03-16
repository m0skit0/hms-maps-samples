package com.demos.maps.gms

import androidx.appcompat.app.AppCompatActivity
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET

data class Directions(
        @SerializedName("geocoded_waypoints")
        val geocodedWaypoints: List<GeocodedWaypoint> = listOf(),
        @SerializedName("routes")
        val routes: List<Route> = listOf(),
        @SerializedName("status")
        val status: String = ""
)

data class GeocodedWaypoint(
        @SerializedName("geocoder_status")
        val geocoderStatus: String = "",
        @SerializedName("place_id")
        val placeId: String = "",
        @SerializedName("types")
        val types: List<String> = listOf()
)

data class Route(
        @SerializedName("bounds")
        val bounds: Bounds = Bounds(),
        @SerializedName("copyrights")
        val copyrights: String = "",
        @SerializedName("legs")
        val legs: List<Leg> = listOf(),
        @SerializedName("overview_polyline")
        val overviewPolyline: OverviewPolyline = OverviewPolyline(),
        @SerializedName("summary")
        val summary: String = "",
        @SerializedName("warnings")
        val warnings: List<Any> = listOf(),
        @SerializedName("waypoint_order")
        val waypointOrder: List<Int> = listOf()
)

data class Bounds(
        @SerializedName("northeast")
        val northeast: Northeast = Northeast(),
        @SerializedName("southwest")
        val southwest: Southwest = Southwest()
)

data class Distance(
        @SerializedName("text")
        val text: String = "",
        @SerializedName("value")
        val value: Int = 0
)

data class Duration(
        @SerializedName("text")
        val text: String = "",
        @SerializedName("value")
        val value: Int = 0
)

data class EndLocation(
        @SerializedName("lat")
        val lat: Double = 0.0,
        @SerializedName("lng")
        val lng: Double = 0.0
)

data class Leg(
        @SerializedName("distance")
        val distance: Distance = Distance(),
        @SerializedName("duration")
        val duration: Duration = Duration(),
        @SerializedName("end_address")
        val endAddress: String = "",
        @SerializedName("end_location")
        val endLocation: EndLocation = EndLocation(),
        @SerializedName("start_address")
        val startAddress: String = "",
        @SerializedName("start_location")
        val startLocation: StartLocation = StartLocation(),
        @SerializedName("steps")
        val steps: List<Step> = listOf(),
        @SerializedName("traffic_speed_entry")
        val trafficSpeedEntry: List<Any> = listOf(),
        @SerializedName("via_waypoint")
        val viaWaypoint: List<Any> = listOf()
)

data class Northeast(
        @SerializedName("lat")
        val lat: Double = 0.0,
        @SerializedName("lng")
        val lng: Double = 0.0
)

data class OverviewPolyline(
        @SerializedName("points")
        val points: String = ""
)

data class Polyline(
        @SerializedName("points")
        val points: String = ""
)

data class Southwest(
        @SerializedName("lat")
        val lat: Double = 0.0,
        @SerializedName("lng")
        val lng: Double = 0.0
)

data class StartLocation(
        @SerializedName("lat")
        val lat: Double = 0.0,
        @SerializedName("lng")
        val lng: Double = 0.0
)

data class Step(
        @SerializedName("distance")
        val distance: Distance = Distance(),
        @SerializedName("duration")
        val duration: Duration = Duration(),
        @SerializedName("end_location")
        val endLocation: EndLocation = EndLocation(),
        @SerializedName("html_instructions")
        val htmlInstructions: String = "",
        @SerializedName("maneuver")
        val maneuver: String = "",
        @SerializedName("polyline")
        val polyline: Polyline = Polyline(),
        @SerializedName("start_location")
        val startLocation: StartLocation = StartLocation(),
        @SerializedName("travel_mode")
        val travelMode: String = ""
)

interface MapsApi {
    @GET("json")
    fun getDirections(origin: String, destination: String, key: String): Call<Directions>
}

class RouteDemoActivity : AppCompatActivity() {
}