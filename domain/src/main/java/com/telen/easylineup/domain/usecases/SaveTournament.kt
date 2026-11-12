/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.hasDateWindow
import com.telen.easylineup.domain.ports.Clock
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.exceptions.AlreadyExistingTournamentException
import com.telen.easylineup.domain.usecases.exceptions.TournamentEndBeforeStartException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import kotlinx.coroutines.withContext

/**
 * Creates a tournament. Its name is trimmed, must not be blank and must not already be taken
 * (a unique index backs this up in the database), and when it has a date window its end must
 * not be before its start.
 */
class SaveTournament(
    private val repository: TournamentRepository,
    private val clock: Clock,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(
        tournament: Tournament
    ): Result<Tournament> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val name = tournament.name.trim()
            if (name.isEmpty()) {
                throw TournamentNameEmptyException()
            }

            if (tournament.hasDateWindow() && tournament.endTime < tournament.startTime) {
                throw TournamentEndBeforeStartException()
            }

            if (repository.getTournamentByName(name) != null) {
                throw AlreadyExistingTournamentException()
            }

            val withCreatedAt = tournament.copy(
                name = name,
                createdAt = clock.nowMillis()
            )
            withCreatedAt.copy(id = repository.insertTournament(withCreatedAt))
        }
    }
}
