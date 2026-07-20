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
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * @property inserted
 * @property updated
 */
class ImportResult(val inserted: IntArray, val updated: IntArray)

class ImportData(
    private val teamDao: TeamRepository,
    private val playerDao: PlayerRepository,
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
            val insertedArray = intArrayOf(0, 0, 0, 0, 0, 0)
            val updatedArray = intArrayOf(0, 0, 0, 0, 0, 0)

            exportBase.teams.forEach { teamExport ->
                val team = processTeam(teamExport, insertedArray, updatedArray, updateIfExists)

                val playerIdMap: MutableMap<String, Long> = mutableMapOf()
                teamExport.players.forEach { playerExport ->
                    val player = processPlayer(
                        playerExport,
                        team.id,
                        insertedArray,
                        updatedArray,
                        updateIfExists
                    )
                    playerIdMap[player.hash ?: UUID.randomUUID().toString()] = player.id
                }

                teamExport.tournaments.forEach { tournamentExport ->
                    val tournament = processTournament(
                        tournamentExport,
                        insertedArray,
                        updatedArray,
                        updateIfExists
                    )

                    tournamentExport.lineups.forEach { lineupExport ->
                        val lineup = processLineup(
                            lineupExport,
                            team.id,
                            tournament.id,
                            playerIdMap,
                            insertedArray,
                            updatedArray,
                            updateIfExists
                        )

                        lineupExport.playerPositions.forEach {
                            processPlayerFieldPosition(
                                it,
                                playerIdMap,
                                lineup.id,
                                insertedArray,
                                updatedArray,
                                updateIfExists
                            )
                        }

                        (lineupExport.playerNumberOverlays ?: listOf()).forEach {
                            processPlayerNumberOverlays(
                                it,
                                playerIdMap,
                                lineup.id,
                                insertedArray,
                                updatedArray,
                                updateIfExists
                            )
                        }
                    }
                }
            }

            ImportResult(insertedArray, updatedArray)
        }
    }

    private suspend fun processTeam(
        teamExport: TeamExport, insertedArray: IntArray, updatedArray: IntArray,
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

        return try {
            val teamDb = teamDao.getTeamByHash(teamExport.id)
            if (updateIfExists) {
                updatedArray[RESULT_TEAMS_INDEX] += 1
                t.id = teamDb.id
                teamDao.updateTeam(t)
                t
            } else {
                teamDb
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            insertedArray[RESULT_TEAMS_INDEX] += 1
            t.id = teamDao.insertTeam(t)
            t
        }
    }

    private suspend fun processPlayer(
        playerExport: PlayerExport, teamId: Long, insertedArray: IntArray, updatedArray: IntArray,
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

        return try {
            val playerDb = playerDao.getPlayerByHash(playerExport.id)
            if (updateIfExists) {
                updatedArray[RESULT_PLAYERS_INDEX] += 1
                p.id = playerDb.id
                playerDao.updatePlayer(p)
                p
            } else {
                playerDb
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            insertedArray[RESULT_PLAYERS_INDEX] += 1
            p.id = playerDao.insertPlayer(p)
            p
        }
    }

    private suspend fun processTournament(
        export: TournamentExport, insertedArray: IntArray, updatedArray: IntArray,
        updateIfExists: Boolean
    ): Tournament {
        val t = Tournament(
            0L, export.name, export.createdAt, export.startTime, export.endTime,
            export.address, export.id
        )

        return try {
            val tournamentDb = tournamentDao.getTournamentByHash(export.id)
            if (updateIfExists) {
                updatedArray[RESULT_TOURNAMENTS_INDEX] += 1
                t.id = tournamentDb.id
                tournamentDao.updateTournament(t)
                t
            } else {
                tournamentDb
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            insertedArray[RESULT_TOURNAMENTS_INDEX] += 1
            t.id = tournamentDao.insertTournament(t)
            t
        }
    }

    private suspend fun processLineup(
        lineup: LineupExport, teamId: Long, tournamentId: Long,
        players: Map<String, Long>,
        insertedArray: IntArray, updatedArray: IntArray,
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

        return try {
            val lineupDb = lineupDao.getLineupByHash(lineup.id)
            if (updateIfExists) {
                updatedArray[RESULT_LINEUPS_INDEX] += 1
                newLineup.id = lineupDb.id
                lineupDao.updateLineup(newLineup)
                newLineup
            } else {
                lineupDb
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            insertedArray[RESULT_LINEUPS_INDEX] += 1
            newLineup.id = lineupDao.insertLineup(newLineup)
            newLineup
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
        export: PlayerPositionExport, players: Map<String, Long>, lineupId: Long,
        insertedArray: IntArray, updatedArray: IntArray,
        updateIfExists: Boolean
    ) {
        val p = PlayerFieldPosition(
            0L, players[export.playerId] ?: 0L, lineupId,
            export.position, export.x, export.y, export.order, export.flags, export.id
        )

        try {
            val positionDb = playerFieldPositionsDao.getPlayerFieldPositionByHash(export.id)
            if (updateIfExists) {
                updatedArray[RESULT_PLAYER_POSITION_INDEX] += 1
                p.id = positionDb.id
                playerFieldPositionsDao.updatePlayerFieldPosition(p)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            insertedArray[RESULT_PLAYER_POSITION_INDEX] += 1
            playerFieldPositionsDao.insertPlayerFieldPosition(p)
        }
    }

    private suspend fun processPlayerNumberOverlays(
        export: PlayerNumberOverlayExport, players: Map<String, Long>, lineupId: Long,
        insertedArray: IntArray, updatedArray: IntArray,
        updateIfExists: Boolean
    ) {
        val p = PlayerNumberOverlay(0L, lineupId, players[export.playerId] ?: 0L, export.number)

        try {
            val overlayDb = playerDao.getPlayerNumberOverlayByHash(export.id)
            if (updateIfExists) {
                updatedArray[RESULT_PLAYER_NUMBER_OVERLAY_INDEX] += 1
                p.id = overlayDb.id
                playerDao.updatePlayerNumberOverlay(p)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            insertedArray[RESULT_PLAYER_NUMBER_OVERLAY_INDEX] += 1
            playerDao.createPlayerNumberOverlay(p)
        }
    }

    companion object {
        const val RESULT_TEAMS_INDEX = 0
        const val RESULT_PLAYERS_INDEX = 1
        const val RESULT_TOURNAMENTS_INDEX = 2
        const val RESULT_LINEUPS_INDEX = 3
        const val RESULT_PLAYER_POSITION_INDEX = 4
        const val RESULT_PLAYER_NUMBER_OVERLAY_INDEX = 5
    }
}
