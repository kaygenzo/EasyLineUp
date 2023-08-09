package com.telen.easylineup.domain.usecases

import android.content.Context
import com.telen.easylineup.domain.R
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.utils.getPositionShortNames
import io.reactivex.rxjava3.core.Single

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

                val topHeaderData = mutableListOf<Pair<String, Int>>()
                val leftHeaderData = mutableListOf<Pair<String, Int>>()
                val mainData = mutableListOf<List<Pair<String, Int>>>()

                val playersIdToPlayerName = mutableMapOf<Long, String>()
                val playerIdToData = mutableMapOf<Long, MutableList<PlayerInLineup>>()

                list.forEach { player ->
                    player.playerID?.let { playerID ->
                        playersIdToPlayerName[playerID] = player.playerName
                            ?: context.getString(R.string.tournament_stats_unknown_player_name)

                        if (!playerIdToData.containsKey(playerID))
                            playerIdToData[playerID] = mutableListOf()

                        playerIdToData[playerID]?.add(player)
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
                    val data = mutableListOf<Pair<String, Int>>()
                    mainData.add(data)

                    //games played
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
                when (TeamType.getTypeById(requestValues.team.type)) {
                    TeamType.SOFTBALL -> {
                        topLeftCell = context.resources
                            .getStringArray(R.array.softball_strategy_array)
                            .toList()
                    }
                    TeamType.BASEBALL -> {
                        topLeftCell = context.resources
                            .getStringArray(R.array.baseball_strategy_array)
                            .toList()
                    }
                    else -> {
                        //nothing to do, just use standard strategy
                    }
                }

                ResponseValue(
                    TournamentStatsUIConfig(
                        leftHeaderData,
                        topHeaderData,
                        mainData,
                        mutableListOf(),
                        topLeftCell
                    )
                )
            }
    }

    class ResponseValue(val uiConfig: TournamentStatsUIConfig) : UseCase.ResponseValue

    class RequestValues(
        val tournament: Tournament,
        val team: Team,
        val strategy: TeamStrategy
    ) : UseCase.RequestValues
}