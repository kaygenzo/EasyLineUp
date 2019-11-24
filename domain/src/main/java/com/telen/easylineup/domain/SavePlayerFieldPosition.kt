package com.telen.easylineup.domain

import com.telen.easylineup.repository.Constants
import com.telen.easylineup.repository.data.*
import io.reactivex.Single

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
                            Constants.ORDER_PITCHER_WHEN_DH
                    }
                    else -> getNextAvailableOrder(requestValues.players)
                }
                PlayerFieldPosition(playerId = requestValues.player.id, lineupId = lineupID, position = requestValues.position.position, order = order)
            } else {
                requestValues.players.first { p -> p.position == requestValues.position.position }.toPlayerFieldPosition()
            }

            playerPosition.apply {
                playerId = requestValues.player.id
                x = requestValues.x
                y = requestValues.y
            }

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
                .filter { it.fieldPositionID > 0 && it.order > 0 }
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
                        val x: Float,
                        val y: Float,
                        val players: List<PlayerWithPosition>,
                        val lineupMode: Int,
                        val isNewPosition: Boolean
    ): UseCase.RequestValues
    inner class ResponseValue: UseCase.ResponseValue
}