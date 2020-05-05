package com.telen.easylineup.domain.usecases

import android.content.Context
import com.telen.easylineup.domain.R
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.Single

internal class GetTournamentStatsForPositionTable(val dao: LineupRepository): UseCase<GetTournamentStatsForPositionTable.RequestValues, GetTournamentStatsForPositionTable.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getAllPlayerPositionsForTournament(requestValues.tournament.id, requestValues.team.id)
                .map { list ->
                    val topHeaderData = mutableListOf<Pair<String, Int>>()
                    val leftHeaderData = mutableListOf<Pair<String, Int>>()
                    val mainData = mutableListOf<List<Pair<String, Int>>>()

                    val playersIdToPlayerName = mutableMapOf<Long, String>()
                    val playerIdToData = mutableMapOf<Long, MutableList<PlayerInLineup>>()

                    list.forEach { player ->
                        player.playerID?.let { playerID ->
                            if(!playersIdToPlayerName.containsKey(playerID)) {
                                playersIdToPlayerName[playerID] = player.playerName  ?:requestValues.context.getString(R.string.tournament_stats_unknown_player_name)
                            }

                            if(!playerIdToData.containsKey(playerID))
                                playerIdToData[playerID] = mutableListOf()

                            playerIdToData[playerID]?.add(player)
                        }
                    }

                    val positionsArray = FieldPosition.getPositionShortNames(requestValues.context, requestValues.team.type)

                    topHeaderData.add(Pair(requestValues.context.getString(R.string.tournament_stats_label_games_played), -1))
                    FieldPosition.values().forEach { fieldPosition ->
                        topHeaderData.add(Pair(positionsArray[fieldPosition.position], fieldPosition.position))
                    }

                    playersIdToPlayerName.forEach { entry ->
                        leftHeaderData.add(Pair(entry.value, 0))
                        val data = mutableListOf<Pair<String, Int>>()
                        mainData.add(data)

                        //games played
                        val gamesCount = playerIdToData[entry.key]
                                ?.filter{ !FieldPosition.isSubstitute(it.position ?: FieldPosition.SUBSTITUTE.position) }
                                ?.size?.toString() ?: "0"
                        data.add(Pair(gamesCount, -1))

                        playerIdToData[entry.key]?.let { positions ->
                            FieldPosition.values().forEach { fieldPosition ->
                                val count = positions.filter { it.position == fieldPosition.position }.size.toString()
                                data.add(Pair(count, fieldPosition.position))
                            }
                        }
                    }

                    ResponseValue(TournamentStatsUIConfig(leftHeaderData, topHeaderData, mainData, mutableListOf(FieldPosition.SUBSTITUTE.position)))
                }
    }

    class ResponseValue(val uiConfig: TournamentStatsUIConfig): UseCase.ResponseValue

    class RequestValues(val tournament: Tournament, val team: Team, val context: Context): UseCase.RequestValues
}