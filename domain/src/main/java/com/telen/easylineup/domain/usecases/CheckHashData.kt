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
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
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
        val teams = teamDao.getTeamsRx()
        val toUpdate: MutableList<Team> = mutableListOf()
        teams.forEach { team ->
            if (team.hash.isNullOrBlank()) {
                team.hash = UUID.randomUUID().toString()
                toUpdate.add(team)
            }
        }
        return teamDao.updateTeamsWithRowCount(toUpdate)
    }

    private suspend fun updatePlayers(): Int {
        val players = playerDao.getPlayers()
        val toUpdate: MutableList<Player> = mutableListOf()
        players.forEach { player ->
            if (player.hash.isNullOrBlank()) {
                player.hash = UUID.randomUUID().toString()
                toUpdate.add(player)
            }
        }
        return playerDao.updatePlayersWithRowCount(toUpdate)
    }

    private suspend fun updateTournaments(): Int {
        val tournaments = tournamentDao.getTournaments()
        val toUpdate: MutableList<Tournament> = mutableListOf()
        tournaments.forEach { tournament ->
            if (tournament.hash.isNullOrBlank()) {
                tournament.hash = UUID.randomUUID().toString()
                toUpdate.add(tournament)
            }
        }
        return tournamentDao.updateTournamentsWithRowCount(toUpdate)
    }

    private suspend fun updateLineups(): Int {
        val lineups = lineupDao.getLineups()
        val toUpdate: MutableList<Lineup> = mutableListOf()
        lineups.forEach { lineup ->
            if (lineup.hash.isNullOrBlank()) {
                lineup.hash = UUID.randomUUID().toString()
                toUpdate.add(lineup)
            }
        }
        return lineupDao.updateLineupsWithRowCount(toUpdate)
    }

    private suspend fun updatePlayerFieldPositions(): Int {
        val playerFieldPositions = playerFieldPositionsDao.getPlayerFieldPositions()
        val toUpdate: MutableList<PlayerFieldPosition> = mutableListOf()
        playerFieldPositions.forEach { playerFieldPosition ->
            if (playerFieldPosition.hash.isNullOrBlank()) {
                playerFieldPosition.hash = UUID.randomUUID().toString()
                toUpdate.add(playerFieldPosition)
            }
        }
        return playerFieldPositionsDao.updatePlayerFieldPositionsWithRowCount(toUpdate)
    }
}
