/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import android.location.Geocoder
import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.model.MapInfo
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.exceptions.AddressNotFoundException
import com.telen.easylineup.domain.usecases.exceptions.MapApiKeyNotFoundException
import com.telen.easylineup.domain.usecases.exceptions.TournamentMapNotFoundException
import io.reactivex.rxjava3.core.Single
import java.io.IOException

class GetTournamentMapLink(
    private val geocoder: Geocoder,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(
        tournament: Tournament,
        apiKey: String?,
        width: Int,
        height: Int
    ): Single<MapInfo> {
        return Single.fromCallable {
            apiKey?.takeIf { it.isNotEmpty() } ?: throw MapApiKeyNotFoundException()
            val address = tournament.address ?: throw AddressNotFoundException()
            val location = getLocationFromAddress(address)
                ?: throw TournamentMapNotFoundException()
            val lat = location.latitude
            val long = location.longitude
            val zoom = 12
            val style = "atlas"
            val basUrl = "https://tile.thunderforest.com/static"
            val link = "$basUrl/$style/$long,$lat,$zoom/${width}x$height.png?apikey=$apiKey"
            MapInfo(link, GeoLocation(lat, long))
        }.subscribeOn(schedulersProvider.io())
    }

    private fun getLocationFromAddress(strAddress: String): AddressLocation? {
        return try {
            val address = geocoder.getFromLocationName(strAddress, 1) ?: return null
            if (address.isNotEmpty()) {
                val location = address[0]
                AddressLocation(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: IOException) {
            null
        }
    }

    /**
     * @property latitude
     * @property longitude
     */
    data class AddressLocation(val latitude: Double, val longitude: Double)
}
