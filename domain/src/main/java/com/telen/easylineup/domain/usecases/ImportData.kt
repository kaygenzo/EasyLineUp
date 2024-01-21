/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
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
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

import java.lang.Exception
import java.util.UUID

internal class ImportData(
    private val teamDao: TeamRepository,
    private val playerDao: PlayerRepository,
    private val tournamentDao: TournamentRepository,
    private val lineupDao: LineupRepository,
    private val playerFieldPositionsDao: PlayerFieldPositionRepository
) : UseCase<ImportData.RequestValues, ImportData.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val insertedArray = intArrayOf(0, 0, 0, 0, 0, 0)
        val updatedArray = intArrayOf(0, 0, 0, 0, 0, 0)
        val updateIfExists = requestValues.updateIfExists
        return Single.just(requestValues.exportBase)
            .flatMapObservable { Observable.fromIterable(it.teams) }
            .flatMapCompletable { teamExport ->
                processTeam(teamExport, insertedArray, updatedArray, updateIfExists)
                    .flatMapCompletable { team ->
                        Observable.fromIterable(teamExport.players)
                            .flatMapSingle { playerExport ->
                                processPlayer(
                                    playerExport,
                                    team.id,
                                    insertedArray,
                                    updatedArray,
                                    updateIfExists
                                )
                            }
                            .toList().map {
                                val map: MutableMap<String, Long> = mutableMapOf()
                                it.forEach { p ->
                                    map[p.hash ?: UUID.randomUUID().toString()] = p.id
                                }
                                map
                            }
                            .flatMapCompletable { playerIdMap ->
                                Observable.fromIterable(teamExport.tournaments)
                                    .flatMapCompletable { tournamentExport ->
                                        processTournament(
                                            tournamentExport,
                                            insertedArray,
                                            updatedArray,
                                            updateIfExists
                                        )
                                            .flatMapCompletable { tournament ->
                                                Observable.fromIterable(tournamentExport.lineups)
                                                    .flatMapCompletable { lineupExport ->
                                                        processLineup(
                                                            lineupExport,
                                                            team.id,
                                                            tournament.id,
                                                            playerIdMap,
                                                            insertedArray,
                                                            updatedArray,
                                                            updateIfExists
                                                        )
                                                            .flatMapCompletable { lineup ->
                                                                val positions =
                                                                    lineupExport.playerPositions
                                                                val playerPositionsCompletable =
                                                                    Observable.fromIterable(
                                                                        positions
                                                                    )
                                                                        .flatMapCompletable {
                                                                            processPlayerFieldPosition(
                                                                                it,
                                                                                playerIdMap,
                                                                                lineup.id,
                                                                                insertedArray,
                                                                                updatedArray,
                                                                                updateIfExists
                                                                            )
                                                                        }
                                                                val overlays =
                                                                    lineupExport.playerNumberOverlays
                                                                        ?: listOf()
                                                                val playerNumberOverlayCompletable =
                                                                    Observable.fromIterable(overlays)
                                                                        .flatMapCompletable {
                                                                            processPlayerNumberOverlays(
                                                                                it,
                                                                                playerIdMap,
                                                                                lineup.id,
                                                                                insertedArray,
                                                                                updatedArray,
                                                                                updateIfExists
                                                                            )
                                                                        }
                                                                playerPositionsCompletable
                                                                    .andThen(
                                                                        playerNumberOverlayCompletable
                                                                    )
                                                            }
                                                    }
                                            }
                                    }
                            }
                    }
            }
            .andThen(Single.just(ResponseValue(insertedArray, updatedArray)))
    }

    private fun processTeam(
        teamExport: TeamExport, insertedArray: IntArray, updatedArray: IntArray,
        updateIfExists: Boolean
    ): Single<Team> {
        val t = Team(
            0L,
            teamExport.name,
            teamExport.image,
            teamExport.type,
            teamExport.main,
            teamExport.id
        )

        return teamDao.getTeamByHash(teamExport.id)
            .flatMap { teamDb ->
                if (updateIfExists) {
                    updatedArray[RESULT_TEAMS_INDEX] += 1
                    t.id = teamDb.id
                    teamDao.updateTeam(t).andThen(Single.just(t))
                } else {
                    Single.just(teamDb)
                }
            }
            .onErrorResumeNext {
                insertedArray[RESULT_TEAMS_INDEX] += 1
                teamDao.insertTeam(t).map {
                    t.id = it
                    t
                }
            }
    }

    private fun processPlayer(
        playerExport: PlayerExport, teamId: Long, insertedArray: IntArray, updatedArray: IntArray,
        updateIfExists: Boolean
    ): Single<Player> {
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
            sex = playerExport.sex,
            hash = playerExport.id
        )

        return playerDao.getPlayerByHash(playerExport.id)
            .flatMap { playerDb ->
                if (updateIfExists) {
                    updatedArray[RESULT_PLAYERS_INDEX] += 1
                    p.id = playerDb.id
                    playerDao.updatePlayer(p).andThen(Single.just(p))
                } else {
                    Single.just(playerDb)
                }
            }
            .onErrorResumeNext {
                insertedArray[RESULT_PLAYERS_INDEX] += 1
                playerDao.insertPlayer(p).map {
                    p.id = it
                    p
                }
            }
    }

    private fun processTournament(
        export: TournamentExport, insertedArray: IntArray, updatedArray: IntArray,
        updateIfExists: Boolean
    ): Single<Tournament> {
        val t = Tournament(
            0L, export.name, export.createdAt, export.startTime, export.endTime,
            export.address, export.id
        )

        return tournamentDao.getTournamentByHash(export.id)
            .flatMap { tournamentDb ->
                if (updateIfExists) {
                    updatedArray[RESULT_TOURNAMENTS_INDEX] += 1
                    t.id = tournamentDb.id
                    tournamentDao.updateTournament(t).andThen(Single.just(t))
                } else {
                    Single.just(tournamentDb)
                }
            }
            .onErrorResumeNext {
                insertedArray[RESULT_TOURNAMENTS_INDEX] += 1
                tournamentDao.insertTournament(t)
                    .map {
                        t.id = it
                        t
                    }
            }
    }

    private fun processLineup(
        lineup: LineupExport, teamId: Long, tournamentId: Long,
        players: Map<String, Long>,
        insertedArray: IntArray, updatedArray: IntArray,
        updateIfExists: Boolean
    ): Single<Lineup> {
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

        return lineupDao.getLineupByHash(lineup.id)
            .flatMap { lineupDb ->
                if (updateIfExists) {
                    updatedArray[RESULT_LINEUPS_INDEX] += 1
                    newLineup.id = lineupDb.id
                    lineupDao.updateLineup(newLineup).andThen(Single.just(newLineup))
                } else {
                    Single.just(lineupDb)
                }
            }
            .onErrorResumeNext {
                insertedArray[RESULT_LINEUPS_INDEX] += 1
                lineupDao.insertLineup(newLineup).map {
                    newLineup.id = it
                    newLineup
                }
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

    private fun processPlayerFieldPosition(
        export: PlayerPositionExport, players: Map<String, Long>, lineupId: Long,
        insertedArray: IntArray, updatedArray: IntArray,
        updateIfExists: Boolean
    ): Completable {
        val p = PlayerFieldPosition(
            0L, players[export.playerId] ?: 0L, lineupId,
            export.position, export.x, export.y, export.order, export.flags, export.id
        )

        return playerFieldPositionsDao.getPlayerFieldPositionByHash(export.id)
            .flatMapCompletable {
                if (updateIfExists) {
                    updatedArray[RESULT_PLAYER_POSITION_INDEX] += 1
                    p.id = it.id
                    playerFieldPositionsDao.updatePlayerFieldPosition(p)
                } else {
                    Completable.complete()
                }
            }
            .onErrorResumeNext {
                insertedArray[RESULT_PLAYER_POSITION_INDEX] += 1
                playerFieldPositionsDao.insertPlayerFieldPosition(p).ignoreElement()
            }
    }

    private fun processPlayerNumberOverlays(
        export: PlayerNumberOverlayExport, players: Map<String, Long>, lineupId: Long,
        insertedArray: IntArray, updatedArray: IntArray,
        updateIfExists: Boolean
    ): Completable {
        val p = PlayerNumberOverlay(0L, lineupId, players[export.playerId] ?: 0L, export.number)

        return playerDao.getPlayerNumberOverlayByHash(export.id)
            .flatMapCompletable {
                if (updateIfExists) {
                    updatedArray[RESULT_PLAYER_NUMBER_OVERLAY_INDEX] += 1
                    p.id = it.id
                    playerDao.updatePlayerNumberOverlay(p)
                } else {
                    Completable.complete()
                }
            }
            .onErrorResumeNext {
                insertedArray[RESULT_PLAYER_NUMBER_OVERLAY_INDEX] += 1
                playerDao.createPlayerNumberOverlay(p)
            }
    }

    /**
     * @property inserted
     * @property updated
     */
    class ResponseValue(val inserted: IntArray, val updated: IntArray) : UseCase.ResponseValue

    /**
     * @property exportBase
     * @property updateIfExists
     */
    class RequestValues(val exportBase: ExportBase, val updateIfExists: Boolean) :
        UseCase.RequestValues

    companion object {
        const val RESULT_TEAMS_INDEX = 0
        const val RESULT_PLAYERS_INDEX = 1
        const val RESULT_TOURNAMENTS_INDEX = 2
        const val RESULT_LINEUPS_INDEX = 3
        const val RESULT_PLAYER_POSITION_INDEX = 4
        const val RESULT_PLAYER_NUMBER_OVERLAY_INDEX = 5
    }
}
