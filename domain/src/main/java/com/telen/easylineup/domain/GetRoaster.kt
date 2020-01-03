package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.RoasterPlayerStatus
import io.reactivex.Single

class GetRoaster(private val dao: PlayerDao, private val lineupDao: LineupDao): UseCase<GetRoaster.RequestValues, GetRoaster.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineupID?.let { lineupID ->
            lineupDao.getLineupByIdSingle(lineupID)
                    .flatMap { lineup ->
                        val roasterIds = stringToRoaster(lineup.roaster)
                        dao.getPlayers(requestValues.teamID).map { players ->
                            // if roasterIds is null, it means that all players are selected
                            val status = roasterIds?.let {
                                if(it.size == players.size) Constants.STATUS_ALL else Constants.STATUS_NONE
                            } ?: Constants.STATUS_ALL
                            ResponseValue(status, players.map { RoasterPlayerStatus(it, roasterIds?.contains(it.id) ?: true) })
                        }
                    }
        } ?: dao.getPlayers(requestValues.teamID).map { ResponseValue(Constants.STATUS_ALL, it.map { RoasterPlayerStatus(it, true) }) }
    }

    class ResponseValue(var status: Int, val players: List<RoasterPlayerStatus>): UseCase.ResponseValue
    class RequestValues(val teamID: Long, val lineupID: Long?): UseCase.RequestValues

    private fun stringToRoaster(roasterString: String?): List<Long>? {
        return roasterString?.let {
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