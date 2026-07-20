/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.LineupRepository
import kotlinx.coroutines.withContext

class UpdateLineupRoster(
    private val lineupRepository: LineupRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineupId: Long, roster: List<RosterPlayerStatus>): Result<Unit> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                val rosterString = rosterToString(roster)
                val lineup = lineupRepository.getLineupByIdSingle(lineupId)
                lineup.roster = rosterString
                lineupRepository.updateLineup(lineup)
            }
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
