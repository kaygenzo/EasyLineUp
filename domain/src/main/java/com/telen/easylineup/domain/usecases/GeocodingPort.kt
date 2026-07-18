package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.GeoLocation

interface GeocodingPort {
    fun getLocationFromAddress(address: String): GeoLocation?
}
