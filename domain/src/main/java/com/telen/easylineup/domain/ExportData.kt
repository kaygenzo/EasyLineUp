package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.*
import com.telen.easylineup.repository.model.*
import com.telen.easylineup.repository.model.export.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface UriChecker {
    fun isNetworkUrl(url: String?): Boolean
}

class ExportData(val teamDao: TeamDao, val playerDao: PlayerDao, val tournamentDao: TournamentDao, val lineupDao: LineupDao,
                 val playerFieldPositionsDao: PlayerFieldPositionsDao): UseCase<ExportData.RequestValues, ExportData.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val teams: MutableList<TeamExport> = mutableListOf()
        val root = ExportBase(teams)
        return teamDao.getTeamsRx()
                .flatMapObservable { Observable.fromIterable(it) }
                .flatMapCompletable { team ->

                    val tournamentsExport = mutableListOf<TournamentExport>()
                    val playersExport = mutableListOf<PlayerExport>()
                    val teamExport = team.toTeamExport(playersExport, tournamentsExport)

                    if(!requestValues.uriChecker.isNetworkUrl(teamExport.image))
                        teamExport.image = null

                    teams.add(teamExport)

                    val playersUUIDMap = mutableMapOf<Long, String?>()

                    playerDao.getPlayers(team.id)
                            .flatMapObservable { Observable.fromIterable(it) }
                            .flatMapCompletable { player ->

                                val playerExport = player.toPlayerExport()

                                if(!requestValues.uriChecker.isNetworkUrl(playerExport.image))
                                    playerExport.image = null

                                playersExport.add(playerExport)

                                playersUUIDMap[player.id] = player.hash
                                Completable.complete()
                            }
                            .andThen(tournamentDao.getTournaments())
                            .flatMapObservable { Observable.fromIterable(it) }
                            .flatMapCompletable { tournament ->

                                val lineupsExport = mutableListOf<LineupExport>()
                                val tournamentExport = tournament.toTournamentExport(lineupsExport)

                                lineupDao.getLineupsForTournamentRx(tournament.id, team.id)
                                        .flatMapObservable {
                                            if(it.isNotEmpty()) {
                                                tournamentsExport.add(tournamentExport)
                                            }
                                            Observable.fromIterable(it)
                                        }
                                        .flatMapCompletable { lineup ->

                                            val positionsExport = mutableListOf<PlayerPositionExport>()
                                            val lineupExport = lineup.toLineupExport(positionsExport)
                                            lineupsExport.add(lineupExport)

                                            playerFieldPositionsDao.getAllPlayerFieldPositionsForLineup(lineup.id)
                                                    .flatMapObservable { Observable.fromIterable(it) }
                                                    .flatMapCompletable {
                                                        val positionExport = it.toPlayerFieldPositionsExport(playersUUIDMap[it.playerId])
                                                        positionsExport.add(positionExport)
                                                        Completable.complete()
                                                    }
                                        }
                            }
                }.andThen(Single.just(ResponseValue(root)))
    }

    class ResponseValue(val exportBase: ExportBase): UseCase.ResponseValue
    class RequestValues(val uriChecker: UriChecker): UseCase.RequestValues
}