/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.TournamentRepository
import kotlinx.coroutines.withContext

class GetTournaments(
    private val dao: TournamentRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(): Result<List<Tournament>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            dao.getTournaments()
        }
    }
}
