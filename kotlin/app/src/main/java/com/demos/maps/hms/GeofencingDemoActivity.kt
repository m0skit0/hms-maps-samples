package com.demos.maps.hms

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.demos.maps.R
import com.huawei.hms.location.*
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.SupportMapFragment
import com.huawei.hms.maps.model.CircleOptions
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MarkerOptions

class GeofencingDemoActivity : AppCompatActivity() {

    inner class GeofencingReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val geofenceData = GeofenceData.getDataFromIntent(intent)
            if (geofenceData.isFailure) {
                toast(R.string.geofencing_event_error)
                return
            }
            // Check if any triggered geofence is ours
            if (geofenceData.convertingGeofenceList.any { it.uniqueId == UNIQUE_ID }) {
                // Show information about the transition in a toast
                when (geofenceData.conversion) {
                    Geofence.ENTER_GEOFENCE_CONVERSION -> toast(R.string.geofencing_enter)
                    Geofence.EXIT_GEOFENCE_CONVERSION -> toast(R.string.geofencing_exit)
                    else -> { /*Do nothing*/ }
                }
            }
        }
    }

    private val RADIUS = 50.0
    private val UNIQUE_ID = javaClass.canonicalName
    private val EXPIRATION = 5 * DateUtils.MINUTE_IN_MILLIS

    private val geofencingService: GeofenceService by lazy {
        LocationServices.getGeofenceService(this)
    }

    private lateinit var map: HuaweiMap
    private lateinit var center: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofencing_demo)

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync {
            map = it
            with (map) {
                isMyLocationEnabled = true
                setOnMapClickListener {
                    center = it
                    drawCircle()
                    clearGeofence()
                    startMonitoring()
                }
            }
        }
    }

    // For more details refer to CircleDemoActivity
    private fun drawCircle() {
        map.clear()
        val marker = MarkerOptions().position(center)
        map.addMarker(marker)
        val circle = CircleOptions().center(center).radius(RADIUS)
        map.addCircle(circle)
    }

    private fun getGeofence(): Geofence =
        Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setUniqueId(UNIQUE_ID)
                // Set the circular region of this geofence.
                .setRoundArea(center.latitude, center.longitude, RADIUS.toFloat())
                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setConversions(Geofence.ENTER_GEOFENCE_CONVERSION or Geofence.EXIT_GEOFENCE_CONVERSION)
                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setValidContinueTime(EXPIRATION)
                .build()

    private fun getGeofencingRequest(): GeofenceRequest =
            GeofenceRequest.Builder()
                    // Specifying INITIAL_TRIGGER_ENTER tells Location services that
                    // GEOFENCE_TRANSITION_ENTER should be triggered if the device is already inside the geofence
                    .setInitConversions(GeofenceRequest.ENTER_INIT_CONVERSION)
                    .createGeofence(getGeofence())
                    .build()

    private fun getPendingIntent(): PendingIntent =
            Intent(this, GeofencingReceiver::class.java).let {
                // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
                // addGeofences() and removeGeofences().
                PendingIntent.getBroadcast(
                        this@GeofencingDemoActivity,
                        0,
                        it,
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

    private fun startMonitoring() {
        geofencingService.createGeofenceList(getGeofencingRequest(), getPendingIntent()).run {
            addOnSuccessListener {
                toast(R.string.geofencing_start_success)
            }
            addOnFailureListener {
                toast(R.string.geofencing_start_failure)
            }
        }
    }

    private fun clearGeofence() {
        geofencingService.deleteGeofenceList(getPendingIntent()).run {
            addOnSuccessListener {
                toast(R.string.geofencing_remove_success)
            }
            addOnFailureListener {
                toast(R.string.geofencing_remove_failure)
            }
        }
    }

    // Clear geofences on Activity exit
    override fun onDestroy() {
        super.onDestroy()
        clearGeofence()
    }

    private fun toast(@StringRes id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show()
    }
}