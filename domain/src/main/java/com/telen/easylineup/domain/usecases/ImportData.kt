package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.*
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.model.export.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.lang.Exception
import java.util.*

internal class ImportData(private val teamDao: TeamRepository, private val playerDao: PlayerRepository, private val tournamentDao: TournamentRepository,
                 private val lineupDao: LineupRepository, private val playerFieldPositionsDao: PlayerFieldPositionRepository): UseCase<ImportData.RequestValues, ImportData.ResponseValue>() {

    companion object {
        const val RESULT_TEAMS_INDEX = 0
        const val RESULT_PLAYERS_INDEX = 1
        const val RESULT_TOURNAMENTS_INDEX = 2
        const val RESULT_LINEUPS_INDEX = 3
        const val RESULT_PLAYER_POSITION_INDEX = 4
    }

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val insertedArray = intArrayOf(0, 0, 0, 0, 0)
        val updatedArray = intArrayOf(0, 0, 0, 0, 0)
        val updateIfExists = requestValues.updateIfExists
        return Single.just(requestValues.exportBase)
                .flatMapObservable { Observable.fromIterable(it.teams) }
                .flatMapCompletable { teamExport ->
                    processTeam(teamExport, insertedArray, updatedArray, updateIfExists)
                            .flatMapCompletable { team ->
                                Observable.fromIterable(teamExport.players)
                                        .flatMapSingle { playerExport ->
                                            processPlayer(playerExport, team.id, insertedArray, updatedArray, updateIfExists)
                                        }
                                        .toList().map {
                                            val map = mutableMapOf<String, Long>()
                                            it.forEach { p ->
                                                map[p.hash ?: UUID.randomUUID().toString()] = p.id
                                            }
                                            map
                                        }
                                        .flatMapCompletable { playerIdMap ->
                                            Observable.fromIterable(teamExport.tournaments)
                                                    .flatMapCompletable { tournamentExport ->
                                                        processTournament(tournamentExport, insertedArray, updatedArray, updateIfExists)
                                                                .flatMapCompletable { tournament ->
                                                                    Observable.fromIterable(tournamentExport.lineups)
                                                                            .flatMapCompletable { lineupExport ->
                                                                                processLineup(lineupExport, team.id, tournament.id, playerIdMap, insertedArray, updatedArray, updateIfExists)
                                                                                        .flatMapCompletable { lineup ->
                                                                                            Observable.fromIterable(lineupExport.playerPositions)
                                                                                                    .flatMapCompletable { processPlayerFieldPosition(it, playerIdMap, lineup.id,
                                                                                                            insertedArray, updatedArray, updateIfExists) }
                                                                                        }
                                                                            }
                                                                }
                                                    }
                                        }
                            }

                }
                .andThen(Single.just(ResponseValue(insertedArray, updatedArray)))
    }

    private fun processTeam(teamExport: TeamExport, insertedArray: IntArray, updatedArray: IntArray,
                            updateIfExists: Boolean): Single<Team> {
        val t = Team(0L, teamExport.name, teamExport.image, teamExport.type, teamExport.main, teamExport.id)

        return teamDao.getTeamByHash(teamExport.id)
                .flatMap { teamDB ->
                    if(updateIfExists) {
                        updatedArray[RESULT_TEAMS_INDEX] += 1
                        t.id = teamDB.id
                        teamDao.updateTeam(t).andThen(Single.just(t))
                    }
                    else
                        Single.just(teamDB)
                }
                .onErrorResumeNext {
                    insertedArray[RESULT_TEAMS_INDEX] += 1
                    teamDao.insertTeam(t).map {
                        t.id = it
                        t
                    }
                }
    }

    private fun processPlayer(playerExport: PlayerExport, teamID: Long, insertedArray: IntArray, updatedArray: IntArray,
                              updateIfExists: Boolean): Single<Player> {
        val licenseNumber = try {
            playerExport.licenseNumber.toLong()
        }
        catch (e: Exception) {
            0L
        }

        val p = Player(id = 0L, teamId = teamID, name = playerExport.name,
                shirtNumber = playerExport.shirtNumber, licenseNumber = licenseNumber,
                image = playerExport.image, positions = playerExport.positions, pitching = playerExport.pitching,
                batting = playerExport.batting, hash = playerExport.id)

        return playerDao.getPlayerByHash(playerExport.id)
                .flatMap { playerDB ->
                    if(updateIfExists) {
                        updatedArray[RESULT_PLAYERS_INDEX] += 1
                        p.id = playerDB.id
                        playerDao.updatePlayer(p).andThen(Single.just(p))
                    }
                    else
                        Single.just(playerDB)
                }
                .onErrorResumeNext {
                    insertedArray[RESULT_PLAYERS_INDEX] += 1
                    playerDao.insertPlayer(p).map {
                        p.id = it
                        p
                    }
                }
    }

    private fun processTournament(export: TournamentExport, insertedArray: IntArray, updatedArray: IntArray,
                                  updateIfExists: Boolean): Single<Tournament> {
        val t = Tournament(0L, export.name,export.createdAt, export.id)

        return tournamentDao.getTournamentByHash(export.id)
                .flatMap { tournamentDB ->
                    if(updateIfExists) {
                        updatedArray[RESULT_TOURNAMENTS_INDEX] += 1
                        t.id = tournamentDB.id
                        tournamentDao.updateTournament(t).andThen(Single.just(t))
                    }
                    else
                        Single.just(tournamentDB)
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

    private fun processLineup(lineup: LineupExport, teamID: Long, tournamentID: Long,
                              players: Map<String, Long>,
                              insertedArray: IntArray, updatedArray: IntArray,
                              updateIfExists: Boolean): Single<Lineup> {
        val l = Lineup(0L, lineup.name, teamID, tournamentID,
                lineup.mode, lineup.eventTime, lineup.createdAt, lineup.editedAt,
                rosterToString(lineup.roster, players), lineup.id)

        return lineupDao.getLineupByHash(lineup.id)
                .flatMap { lineupDB ->
                    if(updateIfExists) {
                        updatedArray[RESULT_LINEUPS_INDEX] += 1
                        l.id = lineupDB.id
                        lineupDao.updateLineup(l).andThen(Single.just(l))
                    }
                    else
                        Single.just(lineupDB)
                }
                .onErrorResumeNext {
                    insertedArray[RESULT_LINEUPS_INDEX] += 1
                    lineupDao.insertLineup(l).map {
                        l.id = it
                        l
                    }
                }
    }

    private fun rosterToString(roster: List<String>?, players: Map<String, Long>): String? {
        val builder = StringBuilder()
        roster?.forEach { hash ->
            players[hash]?.let { playerID ->
                if(builder.isNotEmpty())
                    builder.append(";")
                builder.append(playerID)
            }
        } ?: return null
        return builder.toString()
    }

    private fun processPlayerFieldPosition(export: PlayerPositionExport, players: Map<String, Long>, lineupID: Long,
                                           insertedArray: IntArray, updatedArray: IntArray,
                                           updateIfExists: Boolean): Completable {
        val p = PlayerFieldPosition(0L, players[export.playerID] ?: 0L, lineupID,
                export.position, export.x, export.y, export.order, export.flags, export.id)

        return playerFieldPositionsDao.getPlayerFieldPositionByHash(export.id)
                .flatMapCompletable {
                    if(updateIfExists) {
                        updatedArray[RESULT_PLAYER_POSITION_INDEX] += 1
                        p.id = it.id
                        playerFieldPositionsDao.updatePlayerFieldPosition(p)
                    }
                    else
                        Completable.complete()
                }
                .onErrorResumeNext {
                    insertedArray[RESULT_PLAYER_POSITION_INDEX] += 1
                    playerFieldPositionsDao.insertPlayerFieldPosition(p).ignoreElement()
                }
    }

    class ResponseValue(val inserted: IntArray, val updated: IntArray): UseCase.ResponseValue
    class RequestValues(val exportBase: ExportBase, val updateIfExists: Boolean): UseCase.RequestValues
}