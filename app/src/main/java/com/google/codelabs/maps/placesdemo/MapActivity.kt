package com.google.codelabs.maps.placesdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import com.google.maps.android.data.kml.KmlLayer

data class PlaceOfInterest(
    val name: String,
    val latLng: LatLng
)

class MapActivity : AppCompatActivity() {
    private val plusGridUrl = "https://grid.plus.codes/grid/wms/%d/%d/%d.png"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Confirm we have necessary location permissions
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationPermissionCode
            )
        }

        enableLocation()
    }

    @SuppressLint("MissingPermission")
    private fun initializeMap(userLocation: Location) {
        val mapFragment = supportFragmentManager.findFragmentById(
            R.id.map_fragment
        ) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->

            googleMap.setOnMapLoadedCallback {
                val bounds = LatLngBounds.builder()
                val placesOfInterest = listOf<PlaceOfInterest>(
                    PlaceOfInterest("Googleplex", LatLng(37.422131, -122.084801)),
                    PlaceOfInterest("The Quad", LatLng(37.392129, -122.061707)),
                    PlaceOfInterest("SAP Center", LatLng(37.332778, -121.895))
                )
                placesOfInterest.forEach { place ->
                    bounds.include(place.latLng)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(place.latLng)
                            .title(place.name)
                    )
                }
                // Include user's location in bounds
                bounds.include(LatLng(userLocation.latitude, userLocation.longitude))
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds.build(), 50)
                )
                // Show user's current location
                googleMap.isMyLocationEnabled = true
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

    // TODO XXX this is very WET with LatLongActivity
    @SuppressLint("MissingPermission")
    private fun enableLocation() {
        // Prompt user to enable location settings if needed
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            val states = locationSettingsResponse.locationSettingsStates
            if (states.isLocationPresent) {
                // All location settings are satisfied, client can initialize location requests here
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location == null) {
                            Toast.makeText(this, "Could not get location.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Got last known location successfully
                            initializeMap(location)
                        }
                    }

            }
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, show the user a dialog to enable
                try {
                    exception.startResolutionForResult(this, MainActivity.LOCATION_SETTING_REQUEST)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                    Log.d("Unhandled Exception", sendEx.toString())
                }
            }
        }
    }
}

