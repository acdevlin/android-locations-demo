package com.google.codelabs.maps.placesdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import com.google.maps.android.data.kml.KmlLayer

class MapActivity : AppCompatActivity() {
    private val plusGridUrl = "https://grid.plus.codes/grid/wms/%d/%d/%d.png"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(
            R.id.map_fragment
        ) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            googleMap.setOnMapLoadedCallback {
                val bounds = LatLngBounds.builder()
                val places = listOf<LatLng>(
                    LatLng(37.422131, -122.084801), // 1600 Amphitheatre Pkwy
                    LatLng(37.392129, -122.061707), // The Quad
                    LatLng(37.332778, -121.895) // SAP Center
                )
                places.forEach { place ->
                    bounds.include(place)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(place)
                            .title("Marker on the Map") // TODO replace with more descriptive text
                    )
                }
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds.build(), 50)
                )
            }

            //addMarkers(googleMap)
            //addClusteredMarkers(googleMap)

            // Try adding plus code KML data - see https://grid.plus.codes/
            val layer = KmlLayer(googleMap, R.raw.pluscode, this)
            layer.addLayerToMap()
            // Set a listener for geometry clicked events.
            layer.setOnFeatureClickListener { feature ->
                Log.i(
                    "KML",
                    "Feature clicked: " + feature.id
                )
            }
            val tileProvider: TileProvider = object : UrlTileProvider(256, 256) {
                @Synchronized
                override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                    // The moon tile coordinate system is reversed.  This is not normal.
                    //val reversedY = (1 shl zoom) - y - 1
                    //val s = String.format(Locale.US, moonMapUrl, zoom, x, reversedY)
                    val s = String.format(Locale.US, plusGridUrl, zoom, x, y)
                    var url: URL? = null
                    url = try {
                        URL(s)
                    } catch (e: MalformedURLException) {
                        throw AssertionError(e)
                    }
                    return url
                }
            }
            googleMap.addTileOverlay(TileOverlayOptions().tileProvider(tileProvider))!!
        }
    }
}

