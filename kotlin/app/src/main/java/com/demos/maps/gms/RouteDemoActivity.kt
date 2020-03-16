package com.demos.maps.gms

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.demos.maps.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException

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
    fun getDirections(
            @Query("origin") origin: String,
            @Query("destination") destination: String,
            @Query("key") key: String
    ): Call<Directions>
}

class RouteDemoActivity : AppCompatActivity() {

    private val BASE_URL = "https://maps.googleapis.com/maps/api/directions/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_demo)

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync {
            map = it
        }

        GlobalScope.launch(Dispatchers.Main) {
            askDirectionsAsync().await()?.run {
                setMarkers()
                toast("Got directions!")
            } ?: toast(R.string.error)
        }
    }

    private fun askDirectionsAsync(): Deferred<Directions?> =
            GlobalScope.async(Dispatchers.IO) {
                try {
                    retrofit.create(MapsApi::class.java)
                            .getDirections("Toronto", "Montreal", getString(R.string.google_maps_key))
                            .execute()
                            .run {
                                if (isSuccessful) {
                                    body()
                                } else {
                                    null
                                }
                            }
                } catch (e: IOException) {
                    Log.e(javaClass.simpleName, e.message, e)
                    null
                }
            }

    private fun Directions.setMarkers() {
        routes[0].legs[0].run {
            val startLocation = LatLng(startLocation.lat, startLocation.lng)
            val startMarker = MarkerOptions().position(startLocation)
            map.addMarker(startMarker)

            val endLocation = LatLng(endLocation.lat, endLocation.lng)
            val endMarker = MarkerOptions().position(endLocation)
            map.addMarker(endMarker)

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(startLocation, 5f)
            map.moveCamera(cameraUpdate)
        }
    }


    private fun toast(@StringRes id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}