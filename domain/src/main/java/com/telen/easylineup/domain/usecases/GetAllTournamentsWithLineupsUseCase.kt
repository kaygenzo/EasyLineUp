/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Single

/**
 * @property dao
 */
internal class GetAllTournamentsWithLineupsUseCase(val dao: LineupRepository) :
    UseCase<GetAllTournamentsWithLineupsUseCase.RequestValues,
GetAllTournamentsWithLineupsUseCase.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getAllTournamentsWithLineups(requestValues.filter, requestValues.teamId)
            .map {
                val result: MutableMap<Tournament, MutableList<Lineup>> = mutableMapOf()
                val lineups: MutableMap<Long, Lineup> = mutableMapOf()

                it.forEach { item ->
                    val tournament = item.toTournament()
                    val lineup = item.toLineup()

                    result[tournament] ?: run {
                        result[tournament] = mutableListOf()
                    }

                    if (lineup.id > 0 && lineups[lineup.id] == null) {
                        lineups[lineup.id] = lineup
                        result[tournament]?.add(lineup)
                    }
                }
                result
            }
            .map {
                val list: MutableList<Pair<Tournament, List<Lineup>>> = mutableListOf()
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

    /**
     * @property result
     */
    class ResponseValue(val result: List<Pair<Tournament, List<Lineup>>>) : UseCase.ResponseValue
    /**
     * @property filter
     * @property teamId
     */
    class RequestValues(val filter: String, val teamId: Long) : UseCase.RequestValues
}
