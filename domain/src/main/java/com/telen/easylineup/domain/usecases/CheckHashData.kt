/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import kotlinx.coroutines.withContext
import java.util.UUID

class CheckHashData(
    private val teamDao: TeamRepository,
    private val playerDao: PlayerRepository,
    private val tournamentDao: TournamentRepository,
    private val lineupDao: LineupRepository,
    private val playerFieldPositionsDao: PlayerFieldPositionRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(): Result<IntArray> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            intArrayOf(
                updateTeams(),
                updatePlayers(),
                updateTournaments(),
                updateLineups(),
                updatePlayerFieldPositions()
            )
        }
    }

    private suspend fun updateTeams(): Int {
        val toUpdate = teamDao.getTeams()
            .filter { it.hash.isNullOrBlank() }
            .map { it.copy(hash = UUID.randomUUID().toString()) }
        return teamDao.updateTeamsWithRowCount(toUpdate)
    }

    private suspend fun updatePlayers(): Int {
        val toUpdate = playerDao.getPlayers()
            .filter { it.hash.isNullOrBlank() }
            .map { it.copy(hash = UUID.randomUUID().toString()) }
        return playerDao.updatePlayersWithRowCount(toUpdate)
    }

    private suspend fun updateTournaments(): Int {
        val toUpdate = tournamentDao.getTournaments()
            .filter { it.hash.isNullOrBlank() }
            .map { it.copy(hash = UUID.randomUUID().toString()) }
        return tournamentDao.updateTournamentsWithRowCount(toUpdate)
    }

    private suspend fun updateLineups(): Int {
        val toUpdate = lineupDao.getLineups()
            .filter { it.hash.isNullOrBlank() }
            .map { it.copy(hash = UUID.randomUUID().toString()) }
        return lineupDao.updateLineupsWithRowCount(toUpdate)
    }

    private suspend fun updatePlayerFieldPositions(): Int {
        val toUpdate = playerFieldPositionsDao.getPlayerFieldPositions()
            .filter { it.hash.isNullOrBlank() }
            .map { it.copy(hash = UUID.randomUUID().toString()) }
        return playerFieldPositionsDao.updatePlayerFieldPositionsWithRowCount(toUpdate)
    }
}
