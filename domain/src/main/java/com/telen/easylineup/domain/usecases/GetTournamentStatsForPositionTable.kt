/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import android.content.Context
import com.telen.easylineup.domain.R
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerInLineup
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.TournamentStatsUiConfig
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.utils.getPositionShortNames
import io.reactivex.rxjava3.core.Single

/**
 * @property dao
 */
internal class GetTournamentStatsForPositionTable(
    private val context: Context,
    val dao: LineupRepository
) : UseCase<GetTournamentStatsForPositionTable.RequestValues,
GetTournamentStatsForPositionTable.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getAllPlayerPositionsForTournament(
            requestValues.tournament.id,
            requestValues.team.id
        )
            .map { list ->
                val possiblePositions = requestValues.strategy.positions

                val topHeaderData: MutableList<Pair<String, Int>> = mutableListOf()
                val leftHeaderData: MutableList<Pair<String, Int>> = mutableListOf()
                val mainData: MutableList<List<Pair<String, Int>>> = mutableListOf()

                val playersIdToPlayerName: MutableMap<Long, String> = mutableMapOf()
                val playerIdToData: MutableMap<Long, MutableList<PlayerInLineup>> = mutableMapOf()

                list.forEach { player ->
                    player.playerId?.let { playerId ->
                        playersIdToPlayerName[playerId] = player.playerName
                            ?: context.getString(R.string.tournament_stats_unknown_player_name)

                        if (!playerIdToData.containsKey(playerId)) {
                            playerIdToData[playerId] = mutableListOf()
                        }

                        playerIdToData[playerId]?.add(player)
                    }
                }

                val positionsArray = getPositionShortNames(context, requestValues.team.type)

                topHeaderData
                    .add(Pair(context.getString(R.string.tournament_stats_label_games_played), -1))

                possiblePositions.forEach { fieldPosition ->
                    topHeaderData.add(Pair(positionsArray[fieldPosition.id], fieldPosition.id))
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
                                positions.filter { it.position == fieldPosition.id }.size.toString()
                            data.add(Pair(count, fieldPosition.id))
                        }
                    }
                }

                var topLeftCell: List<String>? = null
                TeamType.getTypeById(requestValues.team.type)
                    .getStrategiesDisplayName(context)?.let {
                        topLeftCell = it.toList()
                    } ?: let { /* nothing to do, just use standard strategy */ }

                ResponseValue(
                    TournamentStatsUiConfig(
                        leftHeaderData,
                        topHeaderData,
                        mainData,
                        mutableListOf(),
                        topLeftCell
                    )
                )
            }
    }

    /**
     * @property uiConfig
     */
    class ResponseValue(val uiConfig: TournamentStatsUiConfig) : UseCase.ResponseValue

    /**
     * @property tournament
     * @property team
     * @property strategy
     */
    class RequestValues(
        val tournament: Tournament,
        val team: Team,
        val strategy: TeamStrategy
    ) : UseCase.RequestValues
}
