/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import io.reactivex.rxjava3.core.Single

class CreateLineup(
    private val lineupsDao: LineupRepository,
    private val getTeam: GetTeam,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(lineup: Lineup, roster: List<RosterPlayerStatus>): Single<Lineup> {
        return Single.defer {
            when {
                "" == lineup.name.trim() -> return@defer Single.error(LineupNameEmptyException())

                lineup.tournamentId <= 0 ->
                    return@defer Single.error(TournamentNameEmptyException())
            }
            val rosterString = if (roster.none { !it.status }) {
                null
            } else {
                rosterToString(roster)
            }
            lineup.roster = rosterString

            getTeam()
                .flatMap { team ->
                    lineup.teamId = team.id
                    lineupsDao.insertLineup(lineup).map {
                        lineup.id = it
                        lineup
                    }
                }
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
