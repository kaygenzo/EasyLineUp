/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import kotlinx.coroutines.withContext

class UpdateLineup(
    private val lineupRepo: LineupRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineup: Lineup): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            if (lineup.name.isBlank()) {
                throw LineupNameEmptyException()
            }
            if (lineup.tournamentId <= 0) {
                throw TournamentNameEmptyException()
            }
            lineupRepo.updateLineup(lineup)
        }
    }
}
