/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.model.export.LineupExport
import com.telen.easylineup.domain.model.export.PlayerExport
import com.telen.easylineup.domain.model.export.PlayerNumberOverlayExport
import com.telen.easylineup.domain.model.export.PlayerPositionExport
import com.telen.easylineup.domain.model.export.TeamExport
import com.telen.easylineup.domain.model.export.TournamentExport
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.ports.repository.PlayerNumberOverlayRepository
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * How many rows of one entity type were inserted vs. updated by an import.
 */
class ImportCounts {
    var inserted: Int = 0
        private set
    var updated: Int = 0
        private set

    fun recordInsert() {
        inserted++
    }

    fun recordUpdate() {
        updated++
    }
}

/**
 * @property teams
 * @property players
 * @property tournaments
 * @property lineups
 * @property playerPositions
 * @property playerNumberOverlays
 */
class ImportResult(
    val teams: ImportCounts = ImportCounts(),
    val players: ImportCounts = ImportCounts(),
    val tournaments: ImportCounts = ImportCounts(),
    val lineups: ImportCounts = ImportCounts(),
    val playerPositions: ImportCounts = ImportCounts(),
    val playerNumberOverlays: ImportCounts = ImportCounts()
)

class ImportData(
    private val teamDao: TeamRepository,
    private val playerDao: PlayerRepository,
    private val playerNumberOverlayDao: PlayerNumberOverlayRepository,
    private val tournamentDao: TournamentRepository,
    private val lineupDao: LineupRepository,
    private val playerFieldPositionsDao: PlayerFieldPositionRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(
        exportBase: ExportBase,
        updateIfExists: Boolean
    ): Result<ImportResult> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val result = ImportResult()

            exportBase.teams.forEach { teamExport ->
                val team = processTeam(teamExport, result.teams, updateIfExists)

                val playerIdMap: MutableMap<String, Long> = mutableMapOf()
                teamExport.players.forEach { playerExport ->
                    val player = processPlayer(
                        playerExport,
                        team.id,
                        result.players,
                        updateIfExists
                    )
                    playerIdMap[player.hash ?: UUID.randomUUID().toString()] = player.id
                }

                teamExport.tournaments.forEach { tournamentExport ->
                    val tournament = processTournament(
                        tournamentExport,
                        result.tournaments,
                        updateIfExists
                    )

                    tournamentExport.lineups.forEach { lineupExport ->
                        val lineup = processLineup(
                            lineupExport,
                            team.id,
                            tournament.id,
                            playerIdMap,
                            result.lineups,
                            updateIfExists
                        )

                        lineupExport.playerPositions.forEach {
                            processPlayerFieldPosition(
                                it,
                                playerIdMap,
                                lineup.id,
                                result.playerPositions,
                                updateIfExists
                            )
                        }

                        (lineupExport.playerNumberOverlays ?: listOf()).forEach {
                            processPlayerNumberOverlays(
                                it,
                                playerIdMap,
                                lineup.id,
                                result.playerNumberOverlays,
                                updateIfExists
                            )
                        }
                    }
                }
            }

            result
        }
    }

    private suspend fun processTeam(
        teamExport: TeamExport,
        counts: ImportCounts,
        updateIfExists: Boolean
    ): Team {
        val t = Team(
            0L,
            teamExport.name,
            teamExport.image,
            teamExport.type,
            teamExport.main,
            teamExport.id
        )

        val teamDb = teamDao.getTeamByHash(teamExport.id)
        return if (teamDb == null) {
            counts.recordInsert()
            t.id = teamDao.insertTeam(t)
            t
        } else if (updateIfExists) {
            counts.recordUpdate()
            t.id = teamDb.id
            teamDao.updateTeam(t)
            t
        } else {
            teamDb
        }
    }

    private suspend fun processPlayer(
        playerExport: PlayerExport,
        teamId: Long,
        counts: ImportCounts,
        updateIfExists: Boolean
    ): Player {
        val licenseNumber = try {
            playerExport.licenseNumber.toLong()
        } catch (e: Exception) {
            0L
        }

        val p = Player(
            id = 0L,
            teamId = teamId,
            name = playerExport.name,
            shirtNumber = playerExport.shirtNumber,
            licenseNumber = licenseNumber,
            image = playerExport.image,
            positions = playerExport.positions,
            pitching = playerExport.pitching,
            batting = playerExport.batting,
            email = playerExport.email,
            phone = playerExport.phone,
            sex = playerExport.sex,
            hash = playerExport.id
        )

        val playerDb = playerDao.getPlayerByHash(playerExport.id)
        return if (playerDb == null) {
            counts.recordInsert()
            p.id = playerDao.insertPlayer(p)
            p
        } else if (updateIfExists) {
            counts.recordUpdate()
            p.id = playerDb.id
            playerDao.updatePlayer(p)
            p
        } else {
            playerDb
        }
    }

    private suspend fun processTournament(
        export: TournamentExport,
        counts: ImportCounts,
        updateIfExists: Boolean
    ): Tournament {
        val t = Tournament(
            0L, export.name, export.createdAt, export.startTime, export.endTime,
            export.address, export.id
        )

        val tournamentDb = tournamentDao.getTournamentByHash(export.id)
        return if (tournamentDb == null) {
            counts.recordInsert()
            t.id = tournamentDao.insertTournament(t)
            t
        } else if (updateIfExists) {
            counts.recordUpdate()
            t.id = tournamentDb.id
            tournamentDao.updateTournament(t)
            t
        } else {
            tournamentDb
        }
    }

    private suspend fun processLineup(
        lineup: LineupExport,
        teamId: Long,
        tournamentId: Long,
        players: Map<String, Long>,
        counts: ImportCounts,
        updateIfExists: Boolean
    ): Lineup {
        val newLineup = Lineup(
            0L,
            lineup.name,
            teamId,
            tournamentId,
            lineup.mode,
            lineup.strategy,
            lineup.extraHitters,
            lineup.eventTime,
            lineup.createdAt,
            lineup.editedAt,
            rosterToString(lineup.roster, players),
            lineup.id
        )

        val lineupDb = lineupDao.getLineupByHash(lineup.id)
        return if (lineupDb == null) {
            counts.recordInsert()
            newLineup.id = lineupDao.insertLineup(newLineup)
            newLineup
        } else if (updateIfExists) {
            counts.recordUpdate()
            newLineup.id = lineupDb.id
            lineupDao.updateLineup(newLineup)
            newLineup
        } else {
            lineupDb
        }
    }

    private fun rosterToString(roster: List<String>?, players: Map<String, Long>): String? {
        val builder = StringBuilder()
        roster?.forEach { hash ->
            players[hash]?.let { playerId ->
                if (builder.isNotEmpty()) {
                    builder.append(";")
                }
                builder.append(playerId)
            }
        } ?: return null
        return builder.toString()
    }

    private suspend fun processPlayerFieldPosition(
        export: PlayerPositionExport,
        players: Map<String, Long>,
        lineupId: Long,
        counts: ImportCounts,
        updateIfExists: Boolean
    ) {
        val p = PlayerFieldPosition(
            0L, players[export.playerId] ?: 0L, lineupId,
            export.position, export.x, export.y, export.order, export.flags, export.id
        )

        val positionDb = playerFieldPositionsDao.getPlayerFieldPositionByHash(export.id)
        if (positionDb == null) {
            counts.recordInsert()
            playerFieldPositionsDao.insertPlayerFieldPosition(p)
        } else if (updateIfExists) {
            counts.recordUpdate()
            p.id = positionDb.id
            playerFieldPositionsDao.updatePlayerFieldPosition(p)
        }
    }

    private suspend fun processPlayerNumberOverlays(
        export: PlayerNumberOverlayExport,
        players: Map<String, Long>,
        lineupId: Long,
        counts: ImportCounts,
        updateIfExists: Boolean
    ) {
        val p = PlayerNumberOverlay(0L, lineupId, players[export.playerId] ?: 0L, export.number)

        val overlayDb = playerNumberOverlayDao.getPlayerNumberOverlayByHash(export.id)
        if (overlayDb == null) {
            counts.recordInsert()
            playerNumberOverlayDao.insertPlayerNumberOverlay(p)
        } else if (updateIfExists) {
            counts.recordUpdate()
            p.id = overlayDb.id
            playerNumberOverlayDao.updatePlayerNumberOverlay(p)
        }
    }
}
