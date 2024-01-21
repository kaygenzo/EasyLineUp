/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Single

internal class UpdateLineupRoster(private val lineupRepository: LineupRepository) :
    UseCase<UpdateLineupRoster.RequestValues, UpdateLineupRoster.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.create {
            val rosterString = rosterToString(requestValues.roster)
            it.onSuccess(rosterString)
        }.flatMapCompletable { rosterString ->
            lineupRepository.getLineupByIdSingle(requestValues.lineupId)
                .map { it.apply { roster = rosterString } }
                .flatMapCompletable { lineupRepository.updateLineup(it) }
        }.andThen(Single.just(ResponseValue()))
    }

    private fun rosterToString(list: List<RosterPlayerStatus>): String {
        val builder = StringBuilder()
        list.forEach {
            if (it.status) {
                if (builder.isNotEmpty()) {
                    builder.append(";")
                }
                builder.append(it.player.id)
            }
        }
        return builder.toString()
    }

    class ResponseValue : UseCase.ResponseValue
    /**
     * @property lineupId
     * @property roster
     */
    class RequestValues(val lineupId: Long, val roster: List<RosterPlayerStatus>) :
        UseCase.RequestValues
}
