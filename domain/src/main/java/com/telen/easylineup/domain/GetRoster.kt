package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.RosterPlayerStatus
import io.reactivex.Single

class GetRoster(private val dao: PlayerDao, private val lineupDao: LineupDao): UseCase<GetRoster.RequestValues, GetRoster.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineupID?.let { lineupID ->
            lineupDao.getLineupByIdSingle(lineupID)
                    .flatMap { lineup ->
                        val rosterIds = stringToRoster(lineup.roster)
                        dao.getPlayers(requestValues.teamID).map { players ->
                            // if rosterIds is null, it means that all players are selected
                            val status = rosterIds?.let {
                                if(it.size == players.size) Constants.STATUS_ALL else Constants.STATUS_PARTIAL
                            } ?: Constants.STATUS_ALL
                            ResponseValue(status, players.map { RosterPlayerStatus(it, rosterIds?.contains(it.id) ?: true) })
                        }
                    }
        } ?: dao.getPlayers(requestValues.teamID).map { ResponseValue(Constants.STATUS_ALL, it.map { RosterPlayerStatus(it, true) }) }
    }

    class ResponseValue(var status: Int, val players: List<RosterPlayerStatus>): UseCase.ResponseValue
    class RequestValues(val teamID: Long, val lineupID: Long?): UseCase.RequestValues

    private fun stringToRoster(rosterString: String?): List<Long>? {
        return rosterString?.let {
            return it.split(";").map {
                try {
                    it.toLong()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    0L
                }
            }.filter { it > 0L } // in case of exception occurred
        }
    }
}