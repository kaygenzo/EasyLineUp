/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class UpdateLineupRoster(
    private val lineupRepository: LineupRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(lineupId: Long, roster: List<RosterPlayerStatus>): Completable {
        return Single.create<String> {
            val rosterString = rosterToString(roster)
            it.onSuccess(rosterString)
        }.flatMapCompletable { rosterString ->
            lineupRepository.getLineupByIdSingle(lineupId)
                .map { it.apply { this.roster = rosterString } }
                .flatMapCompletable { lineupRepository.updateLineup(it) }
        }.subscribeOn(schedulersProvider.io())
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
}
