package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.*
import com.telen.easylineup.repository.model.*
import com.telen.easylineup.repository.model.export.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.lang.Exception
import java.util.*

class ImportData(private val teamDao: TeamDao, private val playerDao: PlayerDao, private val tournamentDao: TournamentDao, private val lineupDao: LineupDao,
                 private val playerFieldPositionsDao: PlayerFieldPositionsDao): UseCase<ImportData.RequestValues, ImportData.ResponseValue>() {

    companion object {
        const val RESULT_TEAMS_INDEX = 0
        const val RESULT_PLAYERS_INDEX = 1
        const val RESULT_TOURNAMENTS_INDEX = 2
        const val RESULT_LINEUPS_INDEX = 3
        const val RESULT_PLAYER_POSITION_INDEX = 4
    }

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val insertedArray = arrayOf(0, 0, 0, 0, 0)
        val updatedArray = arrayOf(0, 0, 0, 0, 0)
        return Single.just(requestValues.exportBase)
                .flatMapObservable { Observable.fromIterable(it.teams) }
                .flatMapCompletable { teamExport ->
                    processTeam(teamExport, insertedArray, updatedArray)
                            .flatMapCompletable { team ->
                                Observable.fromIterable(teamExport.players)
                                        .flatMapSingle { playerExport ->
                                            processPlayer(playerExport, team.id, insertedArray, updatedArray)
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
                                                        processTournament(tournamentExport, insertedArray, updatedArray)
                                                                .flatMapCompletable { tournament ->
                                                                    Observable.fromIterable(tournamentExport.lineups)
                                                                            .flatMapCompletable { lineupExport ->
                                                                                processLineup(lineupExport, team.id, tournament.id, insertedArray, updatedArray)
                                                                                        .flatMapCompletable { lineup ->
                                                                                            Observable.fromIterable(lineupExport.playerPositions)
                                                                                                    .flatMapCompletable { processPlayerFieldPosition(it, playerIdMap, lineup.id,
                                                                                                            insertedArray, updatedArray) }
                                                                                        }
                                                                            }
                                                                }
                                                    }
                                        }
                            }

                }
                .andThen(Single.just(ResponseValue(insertedArray, updatedArray)))
    }

    private fun processTeam(teamExport: TeamExport, insertedArray: Array<Int>, updatedArray: Array<Int>): Single<Team> {
        val t = Team(0L, teamExport.name, teamExport.image, teamExport.type, teamExport.main, teamExport.id)

        return teamDao.getTeamByHash(teamExport.id)
                .flatMap { teamDB ->
                    updatedArray[RESULT_TEAMS_INDEX] += 1
                    t.id = teamDB.id
                    teamDao.updateTeam(t).andThen(Single.just(t))
                }
                .onErrorResumeNext {
                    insertedArray[RESULT_TEAMS_INDEX] += 1
                    teamDao.insertTeam(t).map {
                        t.id = it
                        t
                    }
                }
    }

    private fun processPlayer(playerExport: PlayerExport, teamID: Long, insertedArray: Array<Int>, updatedArray: Array<Int>): Single<Player> {
        val licenseNumber = try {
            playerExport.licenseNumber.toLong()
        }
        catch (e: Exception) {
            0L
        }

        val p = Player(0L, teamID, playerExport.name,
                playerExport.shirtNumber, licenseNumber,
                playerExport.image, playerExport.positions, playerExport.id)

        return playerDao.getPlayerByHash(playerExport.id)
                .flatMap { playerDB ->
                    updatedArray[RESULT_PLAYERS_INDEX] += 1
                    p.id = playerDB.id
                    playerDao.updatePlayer(p).andThen(Single.just(p))
                }
                .onErrorResumeNext {
                    insertedArray[RESULT_PLAYERS_INDEX] += 1
                    playerDao.insertPlayer(p).map {
                        p.id = it
                        p
                    }
                }
    }

    private fun processTournament(export: TournamentExport, insertedArray: Array<Int>, updatedArray: Array<Int>): Single<Tournament> {
        val t = Tournament(0L, export.name,export.createdAt, export.id)

        return tournamentDao.getTournamentByHash(export.id)
                .flatMap { tournamentDB ->
                    updatedArray[RESULT_TOURNAMENTS_INDEX] += 1
                    t.id = tournamentDB.id
                    tournamentDao.updateTournament(t).andThen(Single.just(t))
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
                              insertedArray: Array<Int>, updatedArray: Array<Int>): Single<Lineup> {
        val l = Lineup(0L, lineup.name, teamID, tournamentID,
                lineup.mode, lineup.createdAt, lineup.editedAt,
                lineup.roster, lineup.id)

        return lineupDao.getLineupByHash(lineup.id)
                .flatMap {
                    updatedArray[RESULT_LINEUPS_INDEX] += 1
                    l.id = it.id
                    lineupDao.updateLineup(l).andThen(Single.just(l))
                }
                .onErrorResumeNext {
                    insertedArray[RESULT_LINEUPS_INDEX] += 1
                    lineupDao.insertLineup(l).map {
                        l.id = it
                        l
                    }
                }
    }

    private fun processPlayerFieldPosition(export: PlayerPositionExport, players: Map<String, Long>, lineupID: Long,
                                           insertedArray: Array<Int>, updatedArray: Array<Int>): Completable {
        val p = PlayerFieldPosition(0L, players[export.playerID] ?: 0L, lineupID,
                export.position, export.x, export.y, export.order, export.id)

        return playerFieldPositionsDao.getPlayerFieldPositionByHash(export.id)
                .flatMapCompletable {
                    updatedArray[RESULT_PLAYER_POSITION_INDEX] += 1
                    p.id = it.id
                    playerFieldPositionsDao.updatePlayerFieldPosition(p)
                }
                .onErrorResumeNext {
                    insertedArray[RESULT_PLAYER_POSITION_INDEX] += 1
                    playerFieldPositionsDao.insertPlayerFieldPosition(p).ignoreElement()
                }
    }

    class ResponseValue(val inserted: Array<Int>, val updated: Array<Int>): UseCase.ResponseValue
    class RequestValues(val exportBase: ExportBase): UseCase.RequestValues
}