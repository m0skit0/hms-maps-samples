package com.demos.maps.hms

import android.os.Bundle
import android.view.KeyEvent
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.demos.maps.R
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.SupportMapFragment
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.Coordinate
import com.huawei.hms.site.api.model.SearchStatus
import com.huawei.hms.site.api.model.TextSearchRequest
import com.huawei.hms.site.api.model.TextSearchResponse
import java.io.IOException

class GeocodingDemoActivity : AppCompatActivity() {

    private lateinit var map: HuaweiMap

    private val searchService: SearchService by lazy { SearchServiceFactory.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geocoding_demo_hms)

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync {
            map = it
        }

        findViewById<EditText>(R.id.query)?.let { editText ->
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    map.clear()
                    moveToAddress(editText.text.toString())
                    editText.text.clear()
                    return@setOnKeyListener true
                }
                false
            }
        }
    }

    private fun moveToAddress(address: String) {
        val textSearchRequest = TextSearchRequest().apply {
            query = address
            pageSize = 1
        }
        searchService.textSearch(textSearchRequest, object : SearchResultListener<TextSearchResponse> {
            override fun onSearchError(status: SearchStatus?) {
                status?.errorMessage?.let { toast(it) } ?: toast(R.string.error)

            }

            override fun onSearchResult(response: TextSearchResponse?) {
                response?.sites
                        ?.getOrNull(0)
                        ?.location
                        ?.setMarker()
                        ?.centerCamera() ?: toast(R.string.error)
            }
        })
    }

    private fun Coordinate.setMarker(): Coordinate = apply {
        val position = LatLng(lat, lng)
        val marker = MarkerOptions().position(position)
        map.addMarker(marker)
    }

    private fun Coordinate.centerCamera(): Coordinate = apply {
        val position = LatLng(lat, lng)
        val cameraPosition = CameraUpdateFactory.newLatLng(position)
        map.moveCamera(cameraPosition)
    }

    private fun toast(@StringRes id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}