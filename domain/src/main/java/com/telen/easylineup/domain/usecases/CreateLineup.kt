/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import io.reactivex.rxjava3.core.Single

internal class CreateLineup(private val lineupsDao: LineupRepository) :
    UseCase<CreateLineup.RequestValues, CreateLineup.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.defer {
            requestValues.lineup.let { lineup ->
                when {
                    "" == lineup.name.trim() -> return@let Single.error(LineupNameEmptyException())

                    lineup.tournamentId <= 0 ->
                        return@let Single.error(TournamentNameEmptyException())
                }
                val roster = if (requestValues.roster.none { !it.status }) {
                    null
                } else {
                    rosterToString(requestValues.roster)
                }
                lineup.teamId = requestValues.teamId
                lineup.roster = roster

                lineupsDao.insertLineup(lineup).map {
                    lineup.id = it
                    ResponseValue(lineup)
                }
            }
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

    /**
     * @property lineup
     */
    class ResponseValue(val lineup: Lineup) : UseCase.ResponseValue

    /**
     * @property teamId
     * @property lineup
     * @property roster
     */
    class RequestValues(
        val teamId: Long,
        val lineup: Lineup,
        val roster: List<RosterPlayerStatus>
    ) : UseCase.RequestValues
}
