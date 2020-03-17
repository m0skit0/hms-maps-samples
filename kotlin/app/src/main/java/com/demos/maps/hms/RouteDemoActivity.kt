package com.demos.maps.hms

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.demos.maps.R
import com.google.gson.annotations.SerializedName
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.SupportMapFragment
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.maps.model.PolylineOptions
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.IOException

class RouteDemoActivity : AppCompatActivity() {

    data class RouteServiceRequest(
            @SerializedName("destination")
            val destination: Location = Location(),
            @SerializedName("origin")
            val origin: Location = Location()
    )

    data class Location(
            @SerializedName("lat")
            val lat: Double = 0.0,
            @SerializedName("lng")
            val lng: Double = 0.0
    )

    data class Routes(
            @SerializedName("returnCode")
            val returnCode: String = "",
            @SerializedName("returnDesc")
            val returnDesc: String = "",
            @SerializedName("routes")
            val routes: List<Route> = listOf()
    )

    data class Route(
            @SerializedName("bounds")
            val bounds: Bounds = Bounds(),
            @SerializedName("paths")
            val paths: List<Path> = listOf()
    )

    data class Path(
            @SerializedName("distance")
            val distance: Int = 0,
            @SerializedName("duration")
            val duration: Int = 0,
            @SerializedName("durationInTraffic")
            val durationInTraffic: Int = 0,
            @SerializedName("endLocation")
            val endLocation: Location = Location(),
            @SerializedName("startLocation")
            val startLocation: Location = Location(),
            @SerializedName("steps")
            val steps: List<Step> = listOf()
    )

    data class Bounds(
            @SerializedName("northeast")
            val northeast: Location = Location(),
            @SerializedName("southwest")
            val southwest: Location = Location()
    )

    data class Step(
            @SerializedName("action")
            val action: String = "",
            @SerializedName("distance")
            val distance: Int = 0,
            @SerializedName("duration")
            val duration: Int = 0,
            @SerializedName("endLocation")
            val endLocation: Location = Location(),
            @SerializedName("orientation")
            val orientation: Int = 0,
            @SerializedName("polyline")
            val polyline: List<Location> = listOf(),
            @SerializedName("roadName")
            val roadName: String = "",
            @SerializedName("startLocation")
            val startLocation: Location = Location()
    )

    interface RoutesApi {
        // Each type of route has a different "REST" endpoint
        @POST("driving")
        fun getDirections(
                @Query("key") key: String,
                @Body request: RouteServiceRequest
        ): Call<Routes>
    }

    private val BASE_URL = "https://mapapi.cloud.huawei.com/mapApi/v1/routeService/"

    private val TORONTO = Location(43.6532, -79.3832)
    private val MONTREAL = Location(45.5017, -73.5673)

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    private lateinit var map: HuaweiMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_demo_hms)

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync {
            map = it
        }

        GlobalScope.launch(Dispatchers.Main) {
            askDirectionsAsync().await()
                    ?.setMarkers()
                    ?.drawRoute() ?: toast(R.string.error)
        }
    }

    private fun askDirectionsAsync(): Deferred<Routes?> =
            GlobalScope.async(Dispatchers.IO) {
                try {
                    val request = RouteServiceRequest(TORONTO, MONTREAL)
                    retrofit.create(RoutesApi::class.java)
                            .getDirections(getString(R.string.huawei_maps_key), request)
                            .execute()
                            .run {
                                if (isSuccessful) {
                                    body()
                                } else {
                                    logError(errorBody()?.string())
                                    null
                                }
                            }
                } catch (e: IOException) {
                    logError(e.message, e)
                    null
                }
            }

    private fun Routes.setMarkers(): Routes = apply {
        routes[0].paths[0].run {
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

    private fun Routes.drawRoute(): Routes = apply {
        val polylineOptions = PolylineOptions()
                .geodesic(true)
                .color(resources.getColor(R.color.colorPrimary))
                .addAll(
                        routes[0].paths[0]
                                .steps
                                .map { it.polyline.map { LatLng(it.lat, it.lng) } }
                                .flatten()
                )
        map.addPolyline(polylineOptions)
    }

    private fun toast(@StringRes id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show()
    }

    private fun logError(message: String?, error: Throwable? = null) {
        error?.run {
            Log.e(javaClass.simpleName, message, error)
        } ?: Log.e(javaClass.simpleName, message ?: "")
    }
}