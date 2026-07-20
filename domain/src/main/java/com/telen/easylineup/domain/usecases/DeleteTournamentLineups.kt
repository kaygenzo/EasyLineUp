/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.LineupRepository
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class DeleteTournamentLineups(
    private val lineupDao: LineupRepository,
    private val getTeam: GetTeam,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(tournament: Tournament): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val team = getTeam().await()
            val lineups = lineupDao.getLineupsForTournamentRx(tournament.id, team.id).await()
            lineupDao.deleteLineups(lineups).await()
        }
    }
}
