package com.example.weather

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import android.location.Location
import android.content.Intent
import android.os.*
import com.example.weather.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationRequest
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity2 : AppCompatActivity() {
    var binding: ActivityMainBinding? = null
    private var resultReceiver: ResultReceiver? = null
    val API: String = Constants.API_KEY
    var late: Double = 0.0
    var long: Double = 0.0
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

    inner class weatherTask() : AsyncTask<String, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            val lat: Double = FixedPosition(late)
            val lng: Double = FixedPosition(long)

            println(lng)
            try {
                val CITY: String = "dhaka,bd"
                response = URL("https://api.openweathermap.org/data/2.5/find?lat=${lat}&lon=${lng}&cnt=10&appid=$API").readText(Charsets.UTF_8)
              //  response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(Charsets.UTF_8)
            } catch (e: Exception) {
                response = null
            }
            return response
        }

        private fun FixedPosition(num: Double): Double {
            val number: Double = num
            val number3digits: Double = (number * 1000.0).roundToInt() / 1000.0
            val number2digits: Double = (number3digits * 100.0).roundToInt() / 100.0
            return (number2digits * 10.0).roundToInt() / 10.0
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt: Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: " + SimpleDateFormat(
                    "dd/MM/yyyy hh:mm a",
                    Locale.ENGLISH
                ).format(Date(updatedAt * 1000))
                val temp = main.getString("temp") + "°C"
                val tempMin = "Min Temp: " + main.getString("temp_min") + "°C"
                val tempMax = "Max Temp: " + main.getString("temp_max") + "°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunrise: Long = sys.getLong("sunrise")
                val sunset: Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name") + ", " + sys.getString("country")
                /* Populating extracted data into our views */
                binding.apply {
                    binding!!.currentAddress.text = address
                    binding!!.updateAt.text = updatedAtText
                    binding!!.status.text = weatherDescription.capitalize()
                    binding!!.temp.text = temp
                    binding!!.tempMin.text = tempMin
                    binding!!.tempMax.text = tempMax
                    binding!!.sunrise.text =
                        SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
                    binding!!.sunset.text =
                        SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
                    binding!!.wind.text = windSpeed
                    binding!!.pressure.text = pressure
                    binding!!.humidity.text = humidity
                }

            } catch (e: Exception) {
                // Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
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
                            //  updateValue()
                            late = latitude
                            long = longitude

                            binding!!.textLatLong.text =
                                String.format("Latitude: %s\nLongitude: %s", latitude, longitude)
                            val location = Location("providerNA")
                            location.latitude = latitude
                            location.longitude = longitude
                            weatherTask().execute()
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