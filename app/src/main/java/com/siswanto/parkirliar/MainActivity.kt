package com.siswanto.parkirliar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: LatLng? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Initialize the FloatingActionButton
//        val fab: FloatingActionButton = findViewById(R.id.fab)
//        fab.setOnClickListener {
//            val intent = Intent(this, ReportActivity::class.java)
//            if (userLocation != null) {
//                intent.putExtra("location", "Lat: ${userLocation?.latitude}, Lng: ${userLocation?.longitude}")
//            }
//            startActivity(intent)
//        }

        // Event handler for the report button
        val reportButton: MaterialButton = findViewById(R.id.reportButton)
        reportButton.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            if (userLocation != null) {
                intent.putExtra("location", "Lat: ${userLocation?.latitude}, Lng: ${userLocation?.longitude}")
            }
            startActivity(intent)
        }

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getDeviceLocation()
        }
    }

    private fun getDeviceLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                val location = task.result
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)
                    googleMap.addMarker(MarkerOptions().position(userLocation!!).title("You are here"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f))
                } else {
                    Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                getDeviceLocation()
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
