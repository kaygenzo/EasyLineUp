package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Single

internal class GetAllTournamentsWithLineupsUseCase(val dao: LineupRepository) :
    UseCase<GetAllTournamentsWithLineupsUseCase.RequestValues,
            GetAllTournamentsWithLineupsUseCase.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getAllTournamentsWithLineups(requestValues.filter, requestValues.teamID)
            .map {
                val result: MutableMap<Tournament, MutableList<Lineup>> = mutableMapOf()
                val lineups: MutableMap<Long, Lineup> = mutableMapOf()

                it.forEach { item ->
                    val tournament = item.toTournament()
                    val lineup = item.toLineup()

                    if (result[tournament] == null) {
                        result[tournament] = mutableListOf()
                    }

                    if (lineup.id > 0) {
                        if (lineups[lineup.id] == null) {
                            lineups[lineup.id] = lineup
                            result[tournament]?.add(lineup)
                        }
                    }
                }
                result
            }
            .map {
                val list = mutableListOf<Pair<Tournament, List<Lineup>>>()
                it.forEach { item ->
                    val tournament = item.key
                    val lineups = item.value.sortedByDescending {
                        it.eventTimeInMillis.takeIf { it > 0L } ?: it.createdTimeInMillis
                    }
                    list.add(Pair(tournament, lineups))
                }
                ResponseValue(list)
            }
    }

    class ResponseValue(val result: List<Pair<Tournament, List<Lineup>>>) : UseCase.ResponseValue
    class RequestValues(val filter: String, val teamID: Long) : UseCase.RequestValues
}