/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.model.export.LineupExport
import com.telen.easylineup.domain.model.export.PlayerExport
import com.telen.easylineup.domain.model.export.PlayerNumberOverlayExport
import com.telen.easylineup.domain.model.export.PlayerPositionExport
import com.telen.easylineup.domain.model.export.TeamExport
import com.telen.easylineup.domain.model.export.TournamentExport
import com.telen.easylineup.domain.model.toLineupExport
import com.telen.easylineup.domain.model.toPlayerExport
import com.telen.easylineup.domain.model.toPlayerFieldPositionsExport
import com.telen.easylineup.domain.model.toPlayerNumberOverlayExport
import com.telen.easylineup.domain.model.toTeamExport
import com.telen.easylineup.domain.model.toTournamentExport
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import kotlinx.coroutines.withContext

/**
 * Refreshes hashes for any record missing one, then builds the export payload. Writing that
 * payload to a file is an Android/UI concern handled by the caller (`SettingsViewModel`), not
 * by this UseCase.
 */
class ExportData(
    private val checkHashData: CheckHashData,
    private val teamDao: TeamRepository,
    private val playerDao: PlayerRepository,
    private val tournamentDao: TournamentRepository,
    private val lineupDao: LineupRepository,
    private val playerFieldPositionsDao: PlayerFieldPositionRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(): Result<ExportBase> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            checkHashData().getOrThrow()

            val teams: MutableList<TeamExport> = mutableListOf()
            val root = ExportBase(teams)

            teamDao.getTeamsRx().forEach { team ->
                val tournamentsExport: MutableList<TournamentExport> = mutableListOf()
                val playersExport: MutableList<PlayerExport> = mutableListOf()
                val teamExport = team.toTeamExport(playersExport, tournamentsExport)

                if (!isNetworkUrl(teamExport.image)) {
                    teamExport.image = null
                }

                teams.add(teamExport)

                val playersUuidMap: MutableMap<Long, String?> = mutableMapOf()

                playerDao.getPlayersByTeamId(team.id).forEach { player ->
                    val playerExport = player.toPlayerExport()

                    if (!isNetworkUrl(playerExport.image)) {
                        playerExport.image = null
                    }

                    playersExport.add(playerExport)
                    playersUuidMap[player.id] = player.hash
                }

                tournamentDao.getTournaments().forEach { tournament ->
                    val lineupsExport: MutableList<LineupExport> = mutableListOf()
                    val tournamentExport = tournament.toTournamentExport(lineupsExport)

                    val lineups =
                        lineupDao.getLineupsForTournamentRx(tournament.id, team.id)
                    if (lineups.isNotEmpty()) {
                        tournamentsExport.add(tournamentExport)
                    }

                    lineups.forEach { lineup ->
                        val positionsExport: MutableList<PlayerPositionExport> = mutableListOf()
                        val playerNumberOverlays: MutableList<PlayerNumberOverlayExport> =
                            mutableListOf()
                        val roster = rosterToUuid(playersUuidMap, lineup.roster)
                        val lineupExport = lineup.toLineupExport(
                            positionsExport,
                            playerNumberOverlays,
                            roster
                        )
                        lineupsExport.add(lineupExport)

                        playerFieldPositionsDao.getAllPlayerFieldPositionsForLineup(lineup.id)
                            .forEach {
                                val positionExport = it.toPlayerFieldPositionsExport(
                                    playersUuidMap[it.playerId]
                                )
                                positionsExport.add(positionExport)
                            }

                        playerDao.getPlayersNumberOverlay(lineup.id).forEach {
                            val playerNumberExport = it.toPlayerNumberOverlayExport(
                                playersUuidMap[it.playerId]
                            )
                            playerNumberOverlays.add(playerNumberExport)
                        }
                    }
                }
            }

            root
        }
    }

    private fun isNetworkUrl(url: String?): Boolean {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"))
    }

    private fun rosterToUuid(
        players: Map<Long, String?>,
        roaster: String?
    ): List<String>? {
        return roaster?.run {
            this.split(";")
                .filter { it.isNotBlank() && it.all { c -> c.isDigit() } }
                .map {
                    it.toLong()
                }
                .map { players[it] ?: "" }
                .filter { it.isNotEmpty() }
        }
    }
}
