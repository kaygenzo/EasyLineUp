/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.TransactionRunner
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import kotlinx.coroutines.withContext

/**
 * Deletes the current team's lineups in [Tournament], then the tournament itself once no team
 * has a lineup left in it. Tournaments are shared by every team, so one another team still
 * plays in stays; but one left empty would otherwise stay listed, now that the tournament list
 * shows tournaments without lineups too.
 */
class DeleteTournamentLineups(
    private val lineupDao: LineupRepository,
    private val tournamentDao: TournamentRepository,
    private val getTeam: GetTeam,
    private val transactionRunner: TransactionRunner,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(tournament: Tournament): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val team = getTeam().getOrThrow()
            transactionRunner.inTransaction {
                val lineups = lineupDao.getLineupsForTournament(tournament.id, team.id)
                lineupDao.deleteLineups(lineups)
                if (!lineupDao.hasLineupsForTournament(tournament.id)) {
                    tournamentDao.deleteTournament(tournament)
                }
            }
        }
    }
}
