package com.telen.easylineup.domain.usecases

import android.location.Geocoder
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.exceptions.AddressNotFoundException
import com.telen.easylineup.domain.usecases.exceptions.MapApiKeyNotFoundException
import com.telen.easylineup.domain.usecases.exceptions.TournamentMapNotFoundException
import io.reactivex.rxjava3.core.Single
import java.io.IOException

internal class GetTournamentMapLink(private val geocoder: Geocoder) :
    UseCase<GetTournamentMapLink.RequestValues, GetTournamentMapLink.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.fromCallable {
            with(requestValues) {
                apiKey?.takeIf { it.isNotEmpty() } ?: throw MapApiKeyNotFoundException()
                val address = tournament.address ?: throw AddressNotFoundException()
                val location = getLocationFromAddress(address)
                    ?: throw TournamentMapNotFoundException()
                val lat = location.latitude
                val long = location.longitude
                val zoom = 12
                val style = "atlas"
                val basUrl = "https://tile.thunderforest.com/static"
                val imageWidth = width
                val imageHeight = height
                val link = "$basUrl/$style/$long,$lat,$zoom/${imageWidth}x${imageHeight}.png" +
                        "?apikey=$apiKey"
                ResponseValue(link)
            }
        }
    }

    data class ResponseValue(val mapLink: String) : UseCase.ResponseValue
    data class RequestValues(
        val tournament: Tournament,
        val apiKey: String?,
        val width: Int,
        val height: Int
    ) : UseCase.RequestValues

    data class AddressLocation(val latitude: Double, val longitude: Double)

    private fun getLocationFromAddress(strAddress: String): AddressLocation? {
        return try {
            val address = geocoder.getFromLocationName(strAddress, 1) ?: return null
            if (address.isNotEmpty()) {
                val location = address[0]
                AddressLocation(location.latitude, location.longitude)
            } else null
        } catch (e: IOException) {
            null
        }
    }
}