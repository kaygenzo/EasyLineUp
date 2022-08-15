package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import io.reactivex.rxjava3.core.Single

internal class SavePlayerFieldPosition(private val lineupDao: PlayerFieldPositionRepository): UseCase<SavePlayerFieldPosition.RequestValues, SavePlayerFieldPosition.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineupID?.let { lineupID ->
            val playerPosition = requestValues.players.firstOrNull {
                p -> p.position == requestValues.position.id  && p.position != FieldPosition.SUBSTITUTE.id
            }?.run {
                this.toPlayerFieldPosition()
            } ?: run {
                PlayerFieldPosition(playerId = 0, lineupId = lineupID, position = requestValues.position.id, order = 0)
            }

            playerPosition.apply {
                when (requestValues.position) {
                    FieldPosition.SUBSTITUTE -> {
                        // it depends of the extra hitter size
                        val nextAvailableOrder = PlayerWithPosition.getNextAvailableOrder(requestValues.players, listOf(order))
                        val substitutesBatterSize = requestValues.players.filter { FieldPosition.isSubstitute(it.position) && it.order > 0 }.size
                        // there are some cases where substitutes can me inserted before defense players.
                        // We authorize only maximum extraBatterSize susbstitutes to be batter
                        if(nextAvailableOrder > requestValues.batterSize + requestValues.extraHittersSize || substitutesBatterSize >= requestValues.extraHittersSize) {
                            order = Constants.SUBSTITUTE_ORDER_VALUE
                        }
                    }
                    FieldPosition.PITCHER -> {
                        //if the new position is a pitcher for a baseball team with dh enabled, the batting order is automatically equals to 10
                        if (requestValues.lineupMode == MODE_ENABLED && requestValues.teamType == TeamType.BASEBALL.id) {
                            order = requestValues.strategy.getDesignatedPlayerOrder(requestValues.extraHittersSize)
                            flags = PlayerFieldPosition.FLAG_FLEX
                        }
                    }
                    else -> { }
                }

                playerId = requestValues.player.id

                // we keep the order of the previous position, except if there wasn't
                if(order == 0)
                    order = PlayerWithPosition.getNextAvailableOrder(requestValues.players, listOf(order))

                val coordinate = FieldPosition.getPositionCoordinates(requestValues.position, requestValues.strategy)
                x = coordinate.x
                y = coordinate.y
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
                        val lineupMode: Int,
                        val strategy: TeamStrategy,
                        val batterSize: Int,
                        val extraHittersSize: Int
    ): UseCase.RequestValues
    inner class ResponseValue: UseCase.ResponseValue
}