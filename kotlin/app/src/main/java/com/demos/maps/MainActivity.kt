package com.demos.maps

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.huawei.hms.api.HuaweiApiAvailability

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.huawei_maps)?.run {
            isEnabled = isHMSAvailable()
            setOnClickListener {
                DemoType.HUAWEI.startDemo()
            }
        }

        findViewById<Button>(R.id.google_maps)?.run {
            isEnabled = isGMSAvailable()
            setOnClickListener {
                DemoType.GOOGLE.startDemo()
            }
        }
    }

    private fun isGMSAvailable(): Boolean =
            GoogleApiAvailability
                    .getInstance()
                    .isGooglePlayServicesAvailable(applicationContext) == ConnectionResult.SUCCESS

    private fun isHMSAvailable(): Boolean =
            HuaweiApiAvailability
                    .getInstance()
                    .isHuaweiMobileNoticeAvailable(applicationContext) ==
                    com.huawei.hms.api.ConnectionResult.SUCCESS

    private fun DemoType.startDemo() {
        startActivity(DemoListActivity.launchIntent(this@MainActivity, this))
    }
}