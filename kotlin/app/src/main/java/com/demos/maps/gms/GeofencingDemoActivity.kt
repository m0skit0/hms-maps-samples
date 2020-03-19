package com.demos.maps.gms

import android.Manifest
import android.annotation.SuppressLint
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
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class GeofencingDemoActivity : AppCompatActivity() {

    inner class GeofencingReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent.hasError()) {
                toast(R.string.geofencing_event_error)
                return
            }
            // Check if any triggered geofence is ours
            if (geofencingEvent.triggeringGeofences.any { it.requestId == REQUEST_ID }) {
                // Show information about the transition in a toast
                when (geofencingEvent.geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> toast(R.string.geofencing_enter)
                    Geofence.GEOFENCE_TRANSITION_EXIT -> toast(R.string.geofencing_exit)
                    else -> { /*Do nothing*/ }
                }
            }
        }
    }

    private val RADIUS = 50.0
    private val REQUEST_ID = javaClass.canonicalName
    private val EXPIRATION = 5 * DateUtils.MINUTE_IN_MILLIS

    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(this)
    }

    private lateinit var map: GoogleMap
    private lateinit var center: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofencing_demo)
        enableMyLocation()
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
                .setRequestId(REQUEST_ID)
                // Set the circular region of this geofence.
                .setCircularRegion(center.latitude, center.longitude, RADIUS.toFloat())
                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(EXPIRATION)
                .build()

    private fun getGeofencingRequest(): GeofencingRequest =
            GeofencingRequest.Builder()
                    // Specifying INITIAL_TRIGGER_ENTER tells Location services that
                    // GEOFENCE_TRANSITION_ENTER should be triggered if the device is already inside the geofence
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofences(getGeofence().asList())
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
        geofencingClient.addGeofences(getGeofencingRequest(), getPendingIntent()).run {
            addOnSuccessListener {
                toast(R.string.geofencing_start_success)
            }
            addOnFailureListener {
                toast(R.string.geofencing_start_failure)
            }
        }
    }

    private fun clearGeofence() {
        geofencingClient.removeGeofences(getPendingIntent()).run {
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

    /** Override the onRequestPermissionResult to use EasyPermissions */
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /**
     * enableMyLocation() will enable the location of the map if the user has given permission
     * for the app to access their device location.
     * Android Studio requires an explicit check before setting map.isMyLocationEnabled to true
     * but we are using EasyPermissions to handle it so we can suppress the "MissingPermission"
     * check.
     */
    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(com.demos.maps.hms.REQUEST_CODE_LOCATION)
    private fun enableMyLocation() {
        if (hasLocationPermission()) {
            map.isMyLocationEnabled = true
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.location),
                    REQUEST_CODE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun Geofence.asList(): List<Geofence> = listOf(this)

    private fun toast(@StringRes id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show()
    }
}