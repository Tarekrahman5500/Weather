package com.example.weather

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.weather.MainActivity2.AddressResultReceiver
import android.view.View
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.example.weather.MainActivity2
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import android.location.Location
import android.os.Looper
import android.content.Intent
import android.os.Handler
import android.os.ResultReceiver
import com.example.weather.FetchAddressInternetService
import com.example.weather.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationRequest

class MainActivity2 : AppCompatActivity() {
    var binding: ActivityMainBinding? = null
    private var resultReceiver: ResultReceiver? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        resultReceiver = AddressResultReceiver(Handler())
        binding!!.buttonGetCurrentLocation.setOnClickListener {
            binding!!.progressBar.visibility = View.VISIBLE
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_LOCATION_PERMISSION
                )
            } else {
                currentLocation
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                currentLocation
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // TODO: Consider calling
    //    ActivityCompat#requestPermissions
    // here to request the missing permissions, and then overriding
    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
    //                                          int[] grantResults)
    // to handle the case where the user grants the permission. See the documentation
    // for ActivityCompat#requestPermissions for more details.
    private val currentLocation: Unit
        get() {
            binding!!.progressBar.visibility = View.VISIBLE
            val locationRequest = LocationRequest()
            locationRequest.interval = 10000
            locationRequest.fastestInterval = 3000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        LocationServices.getFusedLocationProviderClient(this@MainActivity2)
                            .removeLocationUpdates(this)
                        if (locationResult.locations.size > 0) {
                            val lastLocationIndex = locationResult.locations.size - 1
                            val latitude = locationResult.locations[lastLocationIndex].latitude
                            val longitude = locationResult.locations[lastLocationIndex].longitude
                            binding!!.textLatLong.text =
                                String.format("Latitude: %s\nLongitude: %s", latitude, longitude)
                            val location = Location("providerNA")
                            location.latitude = latitude
                            location.longitude = longitude
                            fetchAddressFromLatLong(location)
                        } else {
                            binding!!.progressBar.visibility = View.GONE
                        }
                    }
                }, Looper.getMainLooper())
        }

    private fun fetchAddressFromLatLong(location: Location) {
        val intent = Intent(this, FetchAddressInternetService::class.java)
        intent.putExtra(Constants.RECEIVER, resultReceiver)
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location)
        startService(intent)
    }

    private inner class AddressResultReceiver(handler: Handler?) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            super.onReceiveResult(resultCode, resultData)
            if (resultCode == Constants.SUCCESS_RESULT) {
                binding!!.textAddres.text = resultData.getString(Constants.RESULT_DATA_KEY)
            } else {
                Toast.makeText(this@MainActivity2, resultData.getString(Constants.RESULT_DATA_KEY), Toast.LENGTH_SHORT)
                    .show()
            }
            binding!!.progressBar.visibility = View.GONE
        }
    }

    companion object {
        private const val REQUEST_CODE_LOCATION_PERMISSION = 1
    }
}