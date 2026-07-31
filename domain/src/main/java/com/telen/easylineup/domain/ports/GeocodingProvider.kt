package com.telen.easylineup.domain.ports

import com.telen.easylineup.domain.model.GeoLocation

interface GeocodingProvider {
    fun getLocationFromAddress(address: String): GeoLocation?
}
