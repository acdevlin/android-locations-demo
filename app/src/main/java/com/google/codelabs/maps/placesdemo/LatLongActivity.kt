package com.google.codelabs.maps.placesdemo
// Via https://medium.com/@hasperong/get-current-location-with-latitude-and-longtitude-using-kotlin-2ef6c94c7b76

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.codelabs.maps.placesdemo.MainActivity.Companion.LOCATION_SETTING_REQUEST
import com.google.openlocationcode.OpenLocationCode


class LatLongActivity : AppCompatActivity() /*, LocationListener*/ {
    private lateinit var locationManager: LocationManager
    private lateinit var tvGpsLocation: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionCode = 2

    // Provides a normal precision code, approximately 14x14 meters.
    private val CODE_PRECISION_NORMAL = 10

    // Provides a precise precision code, approximately 2x3 meters.
    private val CODE_PRECISION_EXTRA = 11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContentView(R.layout.activity_latlong)
        title = "Offline Plus Codes"

        val button: Button = findViewById(R.id.getLocation)
        button.setOnClickListener { getLocation() }

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
                getLocation()
            }
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, show the user a dialog to enable
                try {
                    exception.startResolutionForResult(this, LOCATION_SETTING_REQUEST)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                    Log.d("Unhandled Exception", sendEx.toString())
                }
            }
        }
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    Toast.makeText(this, "Could not get location.", Toast.LENGTH_SHORT).show()
                } else {
                    // Got last known location successfully
                    onLocationChanged(location)
                }
            }
    }

    private fun onLocationChanged(location: Location) {
        tvGpsLocation = findViewById(R.id.latLongTextView)
        val plusCode = OpenLocationCode(location.latitude, location.longitude, CODE_PRECISION_NORMAL)
        val preciseCode =
            OpenLocationCode(location.latitude, location.longitude, CODE_PRECISION_EXTRA)
        tvGpsLocation.text =
            "Latitude: " + location.latitude + " , Longitude: " + location.longitude + " , PlusCode: " + plusCode + " , PreciseCode: " + preciseCode
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}