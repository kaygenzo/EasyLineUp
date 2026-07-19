/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Single

class GetRoster(
    private val dao: PlayerRepository,
    private val lineupDao: LineupRepository,
    private val getTeam: GetTeam,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(lineupId: Long? = null): Single<TeamRosterSummary> {
        return getTeam()
            .map { it.id }
            .flatMap { teamId ->
                lineupId?.let { id ->
                    val overlays: MutableMap<Long, PlayerNumberOverlay> = mutableMapOf()
                    dao.getPlayersNumberOverlay(id)
                        .flatMap {
                            it.forEach {
                                overlays[it.playerId] = it
                            }
                            lineupDao.getLineupByIdSingle(id)
                        }
                        .flatMap { lineup ->
                            val rosterIds = stringToRoster(lineup.roster)
                            dao.getPlayersByTeamId(teamId).map { players ->
                                // if rosterIds is null, it means that all players are selected
                                val status = rosterIds?.let {
                                    if (it.size == players.size) {
                                        Constants.STATUS_ALL
                                    } else {
                                        Constants.STATUS_PARTIAL
                                    }
                                } ?: Constants.STATUS_ALL
                                TeamRosterSummary(status, players.map {
                                    RosterPlayerStatus(
                                        it,
                                        rosterIds?.contains(it.id) ?: true,
                                        overlays[it.id]
                                    )
                                })
                            }
                        }
                } ?: dao.getPlayersByTeamId(teamId).map {
                    TeamRosterSummary(
                        Constants.STATUS_ALL,
                        it.map { RosterPlayerStatus(it, true, null) })
                }
            }
            .subscribeOn(schedulersProvider.io())
    }

    private fun stringToRoster(rosterString: String?): List<Long>? {
        return rosterString?.let {
            return it.split(";").map {
                try {
                    it.toLong()
                } catch (e: NumberFormatException) {
                    // e.printStackTrace()
                    0L
                }
            }.filter { it > 0L } // in case of exception occurred
        }
    }
}
