package com.telen.easylineup.domain

import android.graphics.PointF
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.UseCase
import com.telen.easylineup.data.*
import com.telen.easylineup.lineup.ORDER_PITCHER_WHEN_DH
import com.telen.easylineup.utils.Constants
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class SavePlayerFieldPosition(private val lineupDao: LineupDao): UseCase<SavePlayerFieldPosition.RequestValues, SavePlayerFieldPosition.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let {
            it.lineupID?.let { lineupID ->
                val playerPosition: PlayerFieldPosition = if (it.isNewPosition) {
                    val order = when (it.position) {
                        FieldPosition.SUBSTITUTE -> Constants.SUBSTITUTE_ORDER_VALUE
                        FieldPosition.PITCHER -> {
                            if (it.lineupMode == MODE_NONE)
                                getNextAvailableOrder(it.players)
                            else
                                ORDER_PITCHER_WHEN_DH
                        }
                        else -> getNextAvailableOrder(it.players)
                    }
                    PlayerFieldPosition(playerId = it.player.id, lineupId = lineupID, position = it.position.position, order = order)
                } else {
                    it.players.first { p -> p.position == it.position.position }.toPlayerFieldPosition()
                }

                playerPosition.apply {
                    playerId = it.player.id
                    x = it.point.x
                    y = it.point.y
                }

                Timber.d("before playerFieldPosition=$playerPosition")

                val task = if (playerPosition.id > 0) {
                    lineupDao.updatePlayerFieldPosition(playerPosition)
                } else {
                    lineupDao.insertPlayerFieldPosition(playerPosition).ignoreElement()
                }

                task.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                        .subscribe({
                            mUseCaseCallback?.onSuccess(ResponseValue())
                        }, {
                            mUseCaseCallback?.onError()
                        })
            } ?: mUseCaseCallback?.onError()
        }
    }

    private fun getNextAvailableOrder(players: List<PlayerWithPosition>): Int {
        var availableOrder = 1
        players
                .filter { it.fieldPositionID > 0 }
                .sortedBy { it.order }
                .forEach {
                    if(it.order == availableOrder)
                        availableOrder++
                    else
                        return availableOrder
                }
        return availableOrder
    }

    class RequestValues(val lineupID: Long?,
                              val player: Player,
                              val position: FieldPosition,
                              val point: PointF,
                              val players: List<PlayerWithPosition>,
                              val lineupMode: Int,
                              val isNewPosition: Boolean
                              ): UseCase.RequestValues
    inner class ResponseValue: UseCase.ResponseValue
}