/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerInLineup
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.TournamentStatsUiConfig
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Single

class GetTournamentStatsForPositionTable(
    private val stringResourcesProvider: StringResourcesProvider,
    private val dao: LineupRepository,
    private val getTeam: GetTeam,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(
        tournament: Tournament,
        strategy: TeamStrategy
    ): Single<TournamentStatsUiConfig> {
        return getTeam()
            .flatMap { team ->
                dao.getAllPlayerPositionsForTournament(tournament.id, team.id)
                    .map { list ->
                        val possiblePositions = strategy.positions

                        val topHeaderData: MutableList<Pair<String, Int>> = mutableListOf()
                        val leftHeaderData: MutableList<Pair<String, Int>> = mutableListOf()
                        val mainData: MutableList<List<Pair<String, Int>>> = mutableListOf()

                        val playersIdToPlayerName: MutableMap<Long, String> = mutableMapOf()
                        val playerIdToData: MutableMap<Long, MutableList<PlayerInLineup>> =
                            mutableMapOf()

                        list.forEach { player ->
                            player.playerId?.let { playerId ->
                                playersIdToPlayerName[playerId] = player.playerName
                                    ?: stringResourcesProvider.unknownPlayerName()

                                if (!playerIdToData.containsKey(playerId)) {
                                    playerIdToData[playerId] = mutableListOf()
                                }

                                playerIdToData[playerId]?.add(player)
                            }
                        }

                        val positionsArray = stringResourcesProvider.positionShortNames(team.type)

                        topHeaderData
                            .add(
                                Pair(
                                    stringResourcesProvider.gamesPlayedLabel(),
                                    -1
                                )
                            )

                        possiblePositions.forEach { fieldPosition ->
                            topHeaderData.add(
                                Pair(positionsArray[fieldPosition.id], fieldPosition.id)
                            )
                        }

                        playersIdToPlayerName.forEach { entry ->
                            leftHeaderData.add(Pair(entry.value, 0))
                            val data: MutableList<Pair<String, Int>> = mutableListOf()
                            mainData.add(data)

                            // games played
                            val gamesCount = playerIdToData[entry.key]
                                ?.filter {
                                    it.position?.let {
                                        FieldPosition.getFieldPositionById(it)?.let {
                                            !it.isSubstitute()
                                        } ?: false
                                    } ?: false
                                }
                                ?.size?.toString() ?: "0"
                            data.add(Pair(gamesCount, -1))

                            playerIdToData[entry.key]?.let { positions ->
                                possiblePositions.forEach { fieldPosition ->
                                    val count =
                                        positions.filter { it.position == fieldPosition.id }
                                            .size.toString()
                                    data.add(Pair(count, fieldPosition.id))
                                }
                            }
                        }

                        var topLeftCell: List<String>? = null
                        stringResourcesProvider.strategyDisplayNames(team.type)?.let {
                            topLeftCell = it.toList()
                        } ?: let { /* nothing to do, just use standard strategy */ }

                        TournamentStatsUiConfig(
                            leftHeaderData,
                            topHeaderData,
                            mainData,
                            mutableListOf(),
                            topLeftCell
                        )
                    }
            }
            .subscribeOn(schedulersProvider.io())
    }
}
