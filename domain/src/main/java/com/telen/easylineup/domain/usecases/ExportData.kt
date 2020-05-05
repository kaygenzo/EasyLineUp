package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.*
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.model.export.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

internal interface ValidationCallback {
    fun isNetworkUrl(url: String?): Boolean
    fun isDigitsOnly(value: String): Boolean
    fun isBlank(value: String): Boolean
}

internal class ExportData(val teamDao: TeamRepository, val playerDao: PlayerRepository, val tournamentDao: TournamentRepository, val lineupDao: LineupRepository,
                 private val playerFieldPositionsDao: PlayerFieldPositionRepository): UseCase<ExportData.RequestValues, ExportData.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val teams: MutableList<TeamExport> = mutableListOf()
        val root = ExportBase(teams)
        return teamDao.getTeamsRx()
                .flatMapObservable { Observable.fromIterable(it) }
                .flatMapCompletable { team ->

                    val tournamentsExport = mutableListOf<TournamentExport>()
                    val playersExport = mutableListOf<PlayerExport>()
                    val teamExport = team.toTeamExport(playersExport, tournamentsExport)

                    if(!requestValues.validator.isNetworkUrl(teamExport.image))
                        teamExport.image = null

                    teams.add(teamExport)

                    val playersUUIDMap = mutableMapOf<Long, String?>()

                    playerDao.getPlayers(team.id)
                            .flatMapObservable { Observable.fromIterable(it) }
                            .flatMapCompletable { player ->

                                val playerExport = player.toPlayerExport()

                                if(!requestValues.validator.isNetworkUrl(playerExport.image))
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
                                            val roster = rosterToUUID(playersUUIDMap, lineup.roster, requestValues.validator)
                                            val lineupExport = lineup.toLineupExport(positionsExport, roster)
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

    private fun rosterToUUID(players: Map<Long, String?>, roaster: String?, validator: ValidationCallback): List<String>? {
        return roaster?.run {
            this.split(";")
                    .filter { validator.isDigitsOnly(it) && !validator.isBlank(it) }
                    .map {
                        it.toLong()
                    }
                    .map { players[it] ?: "" }
                    .filter { it.isNotEmpty() }
        }
    }

    class ResponseValue(val exportBase: ExportBase): UseCase.ResponseValue
    class RequestValues(val validator: ValidationCallback): UseCase.RequestValues
}