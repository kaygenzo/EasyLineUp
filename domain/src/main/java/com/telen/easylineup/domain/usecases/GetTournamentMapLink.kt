/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.GeocodingProvider
import com.telen.easylineup.domain.usecases.exceptions.AddressNotFoundException
import com.telen.easylineup.domain.usecases.exceptions.TournamentMapNotFoundException
import kotlinx.coroutines.withContext

class GetTournamentMapLink(
    private val geocodingProvider: GeocodingProvider,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(tournament: Tournament): Result<GeoLocation> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                val address = tournament.address ?: throw AddressNotFoundException()
                geocodingProvider.getLocationFromAddress(address)
                    ?: throw TournamentMapNotFoundException()
            }
        }
}
