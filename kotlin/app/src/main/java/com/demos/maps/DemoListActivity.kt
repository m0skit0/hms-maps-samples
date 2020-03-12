/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.demos.maps

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.huawei.hms.api.HuaweiApiAvailability

class DemoListActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    companion object {
        private const val DEMO_TYPE = "DEMO_TYPE"
        fun launchIntent(context: Context, type: DemoType): Intent =
            Intent(context, DemoListActivity::class.java).apply {
                putExtra(DEMO_TYPE, type)
            }
    }

    private val demoType: DemoType by lazy { intent?.extras?.get(DEMO_TYPE) as DemoType }

    private val demosList: List<DemoDetails>
        get() =
            when (demoType) {
                DemoType.GOOGLE -> DemoDetailsList.GMS_DEMOS
                DemoType.HUAWEI -> DemoDetailsList.HMS_DEMOS
            }

    private val demosTitle: Int
        get() =
            when (demoType) {
                DemoType.GOOGLE -> R.string.demos_gms
                DemoType.HUAWEI -> R.string.demos_hms
            }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val demo: DemoDetails = parent?.adapter?.getItem(position) as DemoDetails
        startActivity(Intent(this, demo.activityClass))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.demo_list)
        setTitle(demosTitle)

        val listAdapter: ListAdapter = CustomArrayAdapter(this, demosList)

        // Find the view that will show empty message if there is no demo in DemoDetailsList.DEMOS
        val emptyMessage = findViewById<View>(R.id.empty)
        with(findViewById<ListView>(R.id.list)) {
            adapter = listAdapter
            onItemClickListener = this@DemoListActivity
            emptyView = emptyMessage
        }
    }

    /**
     * A custom array adapter that shows a {@link FeatureView} containing details about the demo.
     *
     * @property context current activity
     * @property demos An array containing the details of the demos to be displayed.
     */
    @SuppressLint("ResourceType")
    class CustomArrayAdapter(context: Context, demos: List<DemoDetails>) :
            ArrayAdapter<DemoDetails>(context, R.layout.feature, demos) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            (convertView as? FeatureView ?: FeatureView(context)).apply {
                getItem(position)?.let { demo ->
                    setTitleId(demo.titleId)
                    setDescriptionId(demo.descriptionId)
                    contentDescription = resources.getString(demo.titleId)
                }
            }
    }
}
