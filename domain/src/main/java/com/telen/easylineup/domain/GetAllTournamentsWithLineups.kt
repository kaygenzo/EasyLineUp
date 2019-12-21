package com.telen.easylineup.domain

import android.content.SharedPreferences
import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.model.*
import io.reactivex.Single

class GetAllTournamentsWithLineups(val dao: LineupDao): UseCase<GetAllTournamentsWithLineups.RequestValues, GetAllTournamentsWithLineups.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getAllTournamentsWithLineups(requestValues.filter, requestValues.teamID)
                .map {
                    val result: MutableMap<Tournament, MutableList<Lineup>> = mutableMapOf()
                    val lineups: MutableMap<Long, Lineup> = mutableMapOf()

                    it.forEach { item ->
                        val tournament = item.toTournament()
                        val lineup = item.toLineup()
                        val position = FieldPosition.getFieldPosition(item.position)

                        if(result[tournament] == null) {
                            result[tournament] = mutableListOf()
                        }

                        if(lineup.id > 0) {
                            if(lineups[lineup.id]==null) {
                                lineups[lineup.id] = lineup
                                result[tournament]?.add(lineup)
                            }
                            if(item.fieldPositionID > 0)
                                position?.let {
                                    lineups[lineup.id]?.playerPositions?.add(position)
                                }
                        }
                    }
                    result
                }
                .map {
                    val list = mutableListOf<Pair<Tournament, List<Lineup>>>()
                    it.forEach { item ->
                        val tournament = item.key
                        val lineups = item.value
                        list.add(Pair(tournament, lineups))
                    }
                    ResponseValue(list)
                }
    }

    class ResponseValue(val result: List<Pair<Tournament, List<Lineup>>>): UseCase.ResponseValue
    class RequestValues(val filter: String, val teamID: Long): UseCase.RequestValues
}