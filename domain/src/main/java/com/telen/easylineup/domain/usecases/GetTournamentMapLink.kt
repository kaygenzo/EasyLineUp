/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.GeocodingPort
import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.model.MapInfo
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.exceptions.AddressNotFoundException
import com.telen.easylineup.domain.usecases.exceptions.MapApiKeyNotFoundException
import com.telen.easylineup.domain.usecases.exceptions.TournamentMapNotFoundException
import io.reactivex.rxjava3.core.Single

class GetTournamentMapLink(
    private val geocodingPort: GeocodingPort,
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
            val location = geocodingPort.getLocationFromAddress(address)
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
}
