/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
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
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

internal interface ValidationCallback {
    fun isNetworkUrl(url: String?): Boolean
    fun isDigitsOnly(value: String): Boolean
    fun isBlank(value: String): Boolean
}

/**
 * @property teamDao
 * @property playerDao
 * @property tournamentDao
 * @property lineupDao
 */
internal class ExportData(
    val teamDao: TeamRepository,
    val playerDao: PlayerRepository,
    val tournamentDao: TournamentRepository,
    val lineupDao: LineupRepository,
    private val playerFieldPositionsDao: PlayerFieldPositionRepository
) : UseCase<ExportData.RequestValues, ExportData.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val teams: MutableList<TeamExport> = mutableListOf()
        val root = ExportBase(teams)
        return teamDao.getTeamsRx()
            .flatMapObservable { Observable.fromIterable(it) }
            .flatMapCompletable { team ->

                val tournamentsExport: MutableList<TournamentExport> = mutableListOf()
                val playersExport: MutableList<PlayerExport> = mutableListOf()
                val teamExport = team.toTeamExport(playersExport, tournamentsExport)

                if (!requestValues.validator.isNetworkUrl(teamExport.image)) {
                    teamExport.image = null
                }

                teams.add(teamExport)

                val playersUuidMap: MutableMap<Long, String?> = mutableMapOf()

                playerDao.getPlayersByTeamId(team.id)
                    .flatMapObservable { Observable.fromIterable(it) }
                    .flatMapCompletable { player ->

                        val playerExport = player.toPlayerExport()

                        if (!requestValues.validator.isNetworkUrl(playerExport.image)) {
                            playerExport.image = null
                        }

                        playersExport.add(playerExport)

                        playersUuidMap[player.id] = player.hash
                        Completable.complete()
                    }
                    .andThen(tournamentDao.getTournaments())
                    .flatMapObservable { Observable.fromIterable(it) }
                    .flatMapCompletable { tournament ->

                        val lineupsExport: MutableList<LineupExport> = mutableListOf()
                        val tournamentExport = tournament.toTournamentExport(lineupsExport)

                        lineupDao.getLineupsForTournamentRx(tournament.id, team.id)
                            .flatMapObservable {
                                if (it.isNotEmpty()) {
                                    tournamentsExport.add(tournamentExport)
                                }
                                Observable.fromIterable(it)
                            }
                            .flatMapCompletable { lineup ->

                                val positionsExport: MutableList<PlayerPositionExport> =
                                    mutableListOf()
                                val playerNumberOverlays: MutableList<PlayerNumberOverlayExport> =
                                    mutableListOf()
                                val roster = rosterToUuid(
                                    playersUuidMap,
                                    lineup.roster,
                                    requestValues.validator
                                )
                                val lineupExport = lineup.toLineupExport(
                                    positionsExport,
                                    playerNumberOverlays,
                                    roster
                                )
                                lineupsExport.add(lineupExport)

                                playerFieldPositionsDao.getAllPlayerFieldPositionsForLineup(
                                    lineup.id
                                )
                                    .flatMapObservable { Observable.fromIterable(it) }
                                    .flatMapCompletable {
                                        val positionExport = it.toPlayerFieldPositionsExport(
                                            playersUuidMap[it.playerId]
                                        )
                                        positionsExport.add(positionExport)
                                        Completable.complete()
                                    }
                                    .andThen(playerDao.getPlayersNumberOverlay(lineup.id))
                                    .flatMapObservable { Observable.fromIterable(it) }
                                    .flatMapCompletable {
                                        val playerNumberExport = it.toPlayerNumberOverlayExport(
                                            playersUuidMap[it.playerId]
                                        )
                                        playerNumberOverlays.add(playerNumberExport)
                                        Completable.complete()
                                    }
                            }
                    }
            }.andThen(Single.just(ResponseValue(root)))
    }

    private fun rosterToUuid(
        players: Map<Long, String?>,
        roaster: String?,
        validator: ValidationCallback
    ): List<String>? {
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

    /**
     * @property exportBase
     */
    class ResponseValue(val exportBase: ExportBase) : UseCase.ResponseValue

    /**
     * @property validator
     */
    class RequestValues(val validator: ValidationCallback) : UseCase.RequestValues
}
