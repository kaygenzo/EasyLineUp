/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class GetRoster(
    private val dao: PlayerRepository,
    private val lineupDao: LineupRepository,
    private val getTeam: GetTeam,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineupId: Long? = null): Result<TeamRosterSummary> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val team = getTeam().await()
            val teamId = team.id

            if (lineupId != null) {
                val overlays: MutableMap<Long, PlayerNumberOverlay> = mutableMapOf()
                dao.getPlayersNumberOverlay(lineupId).await().forEach {
                    overlays[it.playerId] = it
                }
                val lineup = lineupDao.getLineupByIdSingle(lineupId).await()
                val rosterIds = stringToRoster(lineup.roster)
                val players = dao.getPlayersByTeamId(teamId).await()
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
            } else {
                val players = dao.getPlayersByTeamId(teamId).await()
                TeamRosterSummary(
                    Constants.STATUS_ALL,
                    players.map { RosterPlayerStatus(it, true, null) }
                )
            }
        }
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
