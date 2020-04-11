package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.*
import io.reactivex.Single

class SavePlayerFieldPosition(private val lineupDao: PlayerFieldPositionsDao): UseCase<SavePlayerFieldPosition.RequestValues, SavePlayerFieldPosition.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineupID?.let { lineupID ->
            val playerPosition = requestValues.players.firstOrNull {
                p -> p.position == requestValues.position.position  && p.position != FieldPosition.SUBSTITUTE.position
            }?.run {
                this.toPlayerFieldPosition()
            } ?: run {
                PlayerFieldPosition(playerId = 0, lineupId = lineupID, position = requestValues.position.position, order = 0)
            }

            playerPosition.apply {
                when (requestValues.position) {
                    FieldPosition.SUBSTITUTE -> {
                        order = Constants.SUBSTITUTE_ORDER_VALUE
                    }
                    FieldPosition.PITCHER -> {
                        if (requestValues.lineupMode == MODE_DISABLED)
                            order = PlayerWithPosition.getNextAvailableOrder(requestValues.players, listOf(order))
                        else {
                            if(requestValues.teamType == TeamType.BASEBALL.id) {
                                order = Constants.ORDER_PITCHER_WHEN_DH
                                flags = PlayerFieldPosition.FLAG_FLEX
                            }
                            else {
                                order = PlayerWithPosition.getNextAvailableOrder(requestValues.players, listOf(order))
                            }
                        }
                    }
                    else -> {
                        order = PlayerWithPosition.getNextAvailableOrder(requestValues.players, listOf(order))
                    }
                }
            }

            playerPosition.apply {
                playerId = requestValues.player.id
                x = requestValues.position.xPercent
                y = requestValues.position.yPercent
            }

            val task = if (playerPosition.id > 0) {
                lineupDao.updatePlayerFieldPosition(playerPosition)
            } else {
                lineupDao.insertPlayerFieldPosition(playerPosition).ignoreElement()
            }

            return task.andThen(Single.just(ResponseValue()))
        } ?: Single.error(Exception("Lineup id is null!"))
    }

    class RequestValues(val lineupID: Long?,
                        val player: Player,
                        val position: FieldPosition,
                        val players: List<PlayerWithPosition>,
                        val teamType: Int,
                        val lineupMode: Int
    ): UseCase.RequestValues
    inner class ResponseValue: UseCase.ResponseValue
}