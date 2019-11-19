package com.telen.easylineup.domain

import com.telen.easylineup.FieldPosition
import com.telen.easylineup.UseCase
import com.telen.easylineup.data.*
import com.telen.easylineup.lineup.LineupStatusDefense

class GetLineupDefenseStatus(private val lineupDao: LineupDao): UseCase<GetLineupDefenseStatus.RequestValues, GetLineupDefenseStatus.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let { req ->

            var lineupMode: Int = MODE_NONE

            req.lineupID?.let { lineupID ->
                lineupDao.getLineupByIdSingle(lineupID)
                        .flatMap {
                            lineupMode = it.mode
                            lineupDao.getAllPlayersWithPositionsForLineupRx(it.id).toSingle()
                        }
                        .subscribe({ players ->
                            val map: MutableMap<Player, FieldPosition?> = mutableMapOf()
                            players.forEach {
                                var position: FieldPosition? = null
                                if(it.fieldPositionID > 0) {
                                    position = FieldPosition.getFieldPosition(it.position)
                                }
                                val player = it.toPlayer()
                                map[player] = position
                            }
                            val result = LineupStatusDefense(map, lineupMode)
                            mUseCaseCallback?.onSuccess(ResponseValue(result, players))
                        }, {
                            mUseCaseCallback?.onError()
                        })
            } ?: mUseCaseCallback?.onError()
        }
    }

    class RequestValues(val lineupID: Long?): UseCase.RequestValues
    class ResponseValue(val lineupStatusDefense: LineupStatusDefense, val players: List<PlayerWithPosition>): UseCase.ResponseValue
}