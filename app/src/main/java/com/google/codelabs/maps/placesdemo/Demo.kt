/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.codelabs.maps.placesdemo

import android.app.Activity
import androidx.annotation.StringRes

enum class Demo(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val activity: Class<out Activity>
) {
    DETAILS_FRAGMENT_DEMO(
        R.string.details_demo_title,
        R.string.details_demo_description,
        DetailsActivity::class.java
    ),
    AUTOCOMPLETE_FRAGMENT_DEMO(
        R.string.autocomplete_fragment_demo_title,
        R.string.autocomplete_fragment_demo_description,
        AutocompleteActivity::class.java
    ),
    CURRENT_FRAGMENT_DEMO(
        R.string.current_demo_title,
        R.string.current_demo_description,
        CurrentPlaceActivity::class.java
    ),
    LATLONG_FRAGMENT_DEMO(
        R.string.latlong_demo_title,
        R.string.latlong_demo_description,
        LatLongActivity::class.java
    ),
    MAP_FRAGMENT_DEMO(
        R.string.map_fragment_demo_title,
        R.string.map_fragment_demo_description,
        MapActivity::class.java
    )
}