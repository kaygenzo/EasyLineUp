package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Tournament
import io.reactivex.Single

internal class GetAllTournamentsWithLineups(val dao: LineupRepository): UseCase<GetAllTournamentsWithLineups.RequestValues, GetAllTournamentsWithLineups.ResponseValue>() {

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
                        val lineups = item.value.sortedByDescending { it.eventTimeInMillis.takeIf { it > 0L } ?: it.createdTimeInMillis }
                        list.add(Pair(tournament, lineups))
                    }
                    ResponseValue(list)
                }
    }

    class ResponseValue(val result: List<Pair<Tournament, List<Lineup>>>): UseCase.ResponseValue
    class RequestValues(val filter: String, val teamID: Long): UseCase.RequestValues
}