/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.exceptions.AlreadyExistingTournamentException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import kotlinx.coroutines.withContext

class SaveTournament(
    private val repository: TournamentRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(
        tournament: Tournament
    ): Result<Tournament> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            if (tournament.name.isEmpty()) {
                throw TournamentNameEmptyException()
            }

            if (repository.getTournamentByName(tournament.name) != null) {
                throw AlreadyExistingTournamentException()
            }

            val withCreatedAt = tournament.copy(createdAt = System.currentTimeMillis())
            withCreatedAt.copy(id = repository.insertTournament(withCreatedAt))
        }
    }
}
