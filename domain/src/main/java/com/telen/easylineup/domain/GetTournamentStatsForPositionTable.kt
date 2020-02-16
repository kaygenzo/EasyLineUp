package com.telen.easylineup.domain

import android.content.Context
import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.model.PlayerInLineup
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Single

class GetTournamentStatsForPositionTable(val dao: LineupDao): UseCase<GetTournamentStatsForPositionTable.RequestValues, GetTournamentStatsForPositionTable.ResponseValue>() {

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

                    val positionsArray = requestValues.context.resources.getStringArray(R.array.field_positions_list)

                    topHeaderData.add(Pair(requestValues.context.getString(R.string.tournament_stats_label_games_played), -1))
                    FieldPosition.values().forEach { fieldPosition ->
                        topHeaderData.add(Pair(positionsArray[fieldPosition.position], fieldPosition.position))
                    }

                    playersIdToPlayerName.forEach { entry ->
                        leftHeaderData.add(Pair(entry.value, 0))
                        val data = mutableListOf<Pair<String, Int>>()
                        mainData.add(data)

                        //games played
                        val gamesCount = playerIdToData[entry.key]?.size?.toString() ?: "0"
                        data.add(Pair(gamesCount, -1))

                        playerIdToData[entry.key]?.let { positions ->
                            FieldPosition.values().forEach { fieldPosition ->
                                val count = positions.filter { it.position == fieldPosition.position }.size.toString()
                                data.add(Pair(count, fieldPosition.position))
                            }
                        }
                    }

                    ResponseValue(leftHeaderData, topHeaderData, mainData)
                }
    }

    class ResponseValue(val leftHeader: List<Pair<String, Int>>,
                        val topHeader: List<Pair<String, Int>>,
                        val mainTable: List<List<Pair<String, Int>>>): UseCase.ResponseValue

    class RequestValues(val tournament: Tournament, val team: Team, val context: Context): UseCase.RequestValues
}