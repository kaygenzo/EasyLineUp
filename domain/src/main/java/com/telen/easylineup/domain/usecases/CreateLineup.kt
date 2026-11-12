/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.ports.Clock
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import kotlinx.coroutines.withContext

class CreateLineup(
    private val lineupsDao: LineupRepository,
    private val getTeam: GetTeam,
    private val clock: Clock,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineup: Lineup, roster: List<RosterPlayerStatus>): Result<Lineup> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                if ("" == lineup.name.trim()) {
                    throw LineupNameEmptyException()
                }
                if (lineup.tournamentId <= 0) {
                    throw TournamentNameEmptyException()
                }

                val rosterString = if (roster.none { !it.status }) {
                    null
                } else {
                    rosterToString(roster)
                }

                val team = getTeam().getOrThrow()
                val now = clock.nowMillis()
                val toInsert = lineup.copy(
                    roster = rosterString,
                    teamId = team.id,
                    createdTimeInMillis = now,
                    editedTimeInMillis = now
                )
                toInsert.copy(id = lineupsDao.insertLineup(toInsert))
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
