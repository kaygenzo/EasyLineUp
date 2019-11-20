package com.telen.easylineup.domain

import android.graphics.PointF
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.UseCase
import com.telen.easylineup.data.*
import com.telen.easylineup.lineup.ORDER_PITCHER_WHEN_DH
import com.telen.easylineup.utils.Constants
import io.reactivex.Single
import timber.log.Timber

class SavePlayerFieldPosition(private val lineupDao: LineupDao): UseCase<SavePlayerFieldPosition.RequestValues, SavePlayerFieldPosition.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineupID?.let { lineupID ->
            val playerPosition: PlayerFieldPosition = if (requestValues.isNewPosition) {
                val order = when (requestValues.position) {
                    FieldPosition.SUBSTITUTE -> Constants.SUBSTITUTE_ORDER_VALUE
                    FieldPosition.PITCHER -> {
                        if (requestValues.lineupMode == MODE_NONE)
                            getNextAvailableOrder(requestValues.players)
                        else
                            ORDER_PITCHER_WHEN_DH
                    }
                    else -> getNextAvailableOrder(requestValues.players)
                }
                PlayerFieldPosition(playerId = requestValues.player.id, lineupId = lineupID, position = requestValues.position.position, order = order)
            } else {
                requestValues.players.first { p -> p.position == requestValues.position.position }.toPlayerFieldPosition()
            }

            playerPosition.apply {
                playerId = requestValues.player.id
                x = requestValues.point.x
                y = requestValues.point.y
            }

            Timber.d("before playerFieldPosition=$playerPosition")

            val task = if (playerPosition.id > 0) {
                lineupDao.updatePlayerFieldPosition(playerPosition)
            } else {
                lineupDao.insertPlayerFieldPosition(playerPosition).ignoreElement()
            }

            return task.andThen(Single.just(ResponseValue()))
        } ?: Single.error(Exception("Lineup id is null!"))
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