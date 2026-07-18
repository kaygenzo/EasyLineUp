package com.telen.easylineup.utils

import android.content.Context
import android.location.Geocoder
import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.usecases.GeocodingPort
import java.io.IOException

class GeocodingPortImpl(context: Context) : GeocodingPort {
    private val geocoder = Geocoder(context)

    override fun getLocationFromAddress(address: String): GeoLocation? {
        return try {
            geocoder.getFromLocationName(address, 1)
                ?.firstOrNull()
                ?.let { GeoLocation(it.latitude, it.longitude) }
        } catch (e: IOException) {
            null
        }
    }
}
