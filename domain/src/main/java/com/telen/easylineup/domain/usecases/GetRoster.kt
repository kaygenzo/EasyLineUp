package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.Single

internal class GetRoster(private val dao: PlayerRepository, private val lineupDao: LineupRepository): UseCase<GetRoster.RequestValues, GetRoster.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineupID?.let { lineupID ->
            val overlays = mutableMapOf<Long, PlayerNumberOverlay>()
            dao.getPlayersNumberOverlay(lineupID)
                    .flatMap {
                        it.forEach {
                            overlays[it.playerID] = it
                        }
                        lineupDao.getLineupByIdSingle(lineupID)
                    }
                    .flatMap { lineup ->
                        val rosterIds = stringToRoster(lineup.roster)
                        dao.getPlayers(requestValues.teamID).map { players ->
                            // if rosterIds is null, it means that all players are selected
                            val status = rosterIds?.let {
                                if(it.size == players.size) Constants.STATUS_ALL else Constants.STATUS_PARTIAL
                            } ?: Constants.STATUS_ALL
                            ResponseValue(TeamRosterSummary(status, players.map {
                                RosterPlayerStatus(it, rosterIds?.contains(it.id) ?: true, overlays[it.id])
                            }))
                        }
                    }
        } ?: dao.getPlayers(requestValues.teamID).map {
            ResponseValue(TeamRosterSummary(Constants.STATUS_ALL, it.map { RosterPlayerStatus(it, true, null) }))
        }
    }

    class ResponseValue(val summary: TeamRosterSummary): UseCase.ResponseValue
    class RequestValues(val teamID: Long, val lineupID: Long?): UseCase.RequestValues

    private fun stringToRoster(rosterString: String?): List<Long>? {
        return rosterString?.let {
            return it.split(";").map {
                try {
                    it.toLong()
                } catch (e: NumberFormatException) {
                    //e.printStackTrace()
                    0L
                }
            }.filter { it > 0L } // in case of exception occurred
        }
    }
}