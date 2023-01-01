package com.google.codelabs.maps.placesdemo
// Via https://medium.com/@hasperong/get-current-location-with-latitude-and-longtitude-using-kotlin-2ef6c94c7b76

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.openlocationcode.OpenLocationCode
import android.provider.Settings
import android.system.Os.accept
import kotlinx.coroutines.NonCancellable.cancel


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
        button.setOnClickListener {
            getLocation()
        }
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if(location == null) {
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        promptEnableLocation()
                    }
                    else {
                        Toast.makeText(this, "Could not get location.", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    // Got last known location successfully
                    onLocationChanged(location)
                }
            }
    }

    private fun promptEnableLocation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("GPS is Turned Off!")
            .setMessage("Please enable location services to continue.")
            .setPositiveButton("YES") { dialog, whichButton ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("NO") { dialog, whichButton ->
                Toast.makeText(this, "Could not get location.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun onLocationChanged(location: Location) {
        tvGpsLocation = findViewById(R.id.latLongTextView)
        val plusCode = OpenLocationCode.encode(location.latitude, location.longitude)
        val preciseCode = OpenLocationCode(location.latitude, location.longitude, CODE_PRECISION_EXTRA)
        tvGpsLocation.text = "Latitude: " + location.latitude + " , Longitude: " + location.longitude + " , PlusCode: " + plusCode + " , PreciseCode: " + preciseCode
    }

    /*
    override fun onLocationChanged(location: Location) {
        tvGpsLocation = findViewById(R.id.latLongTextView)
        val plusCode = OpenLocationCode.encode(location.latitude, location.longitude)
        tvGpsLocation.text = "Latitude: " + location.latitude + " , Longitude: " + location.longitude + " , PlusCode: " + plusCode
    }

     */

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}