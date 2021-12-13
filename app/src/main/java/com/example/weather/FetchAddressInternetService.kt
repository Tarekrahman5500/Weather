package com.example.weather

import android.app.IntentService
import android.content.Intent
import android.location.Location
import android.location.Geocoder
import android.location.Address
import java.lang.Exception
import android.text.TextUtils
import android.os.Bundle
import android.os.ResultReceiver
import java.util.*

class FetchAddressInternetService : IntentService("FetchAddressInternetService") {
    private var resultReceiver: ResultReceiver? = null
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            var errorMessage = ""
            resultReceiver = intent.getParcelableExtra(Constants.RECEIVER)
            val location = intent.getParcelableExtra<Location>(Constants.LOCATION_DATA_EXTRA) ?: return
            val geocoder = Geocoder(this, Locale.getDefault())
            var addresses: List<Address>? = null
            try {
                addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            } catch (e: Exception) {
                errorMessage = e.message.toString()
            }
            if (addresses == null || addresses.isEmpty()) {
                deliveryResultReceiver(Constants.FAILURE_RESULT, errorMessage)
            } else {
                val address = addresses[0]
                val addressFragments = ArrayList<String?>()
                for (i in 0..address.maxAddressLineIndex) {
                    addressFragments.add(address.getAddressLine(i))
                }
                deliveryResultReceiver(
                    Constants.SUCCESS_RESULT,
                    TextUtils.join(
                        Objects.requireNonNull(System.getProperty("line.separator")),
                        addressFragments
                    )
                )
            }
        }
    }

    private fun deliveryResultReceiver(resultCode: Int, addressMessage: String?) {
        val bundle = Bundle()
        bundle.putString(Constants.RESULT_DATA_KEY, addressMessage)
        resultReceiver!!.send(resultCode, bundle)
    }
}