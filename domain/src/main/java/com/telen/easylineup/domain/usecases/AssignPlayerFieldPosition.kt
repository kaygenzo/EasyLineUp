package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.*
import io.reactivex.rxjava3.core.Single

internal class AssignPlayerFieldPosition :
    UseCase<AssignPlayerFieldPosition.RequestValues, AssignPlayerFieldPosition.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.defer {
            val players = requestValues.players
            val lineup = requestValues.lineup
            val lineupMode = lineup.mode
            val strategy = TeamStrategy.getStrategyById(lineup.strategy)
            val batterSize = strategy.batterSize
            val extraHittersSize = lineup.extraHitters
            val position = requestValues.position
            val player = requestValues.player
            val teamType = requestValues.teamType

            val otherPlayerPosition = players.firstOrNull {
                // another player is already on the same position
                it.position == position.id && !it.isSubstitute()
            }
            val playerPosition = players.firstOrNull {
                it.playerID == player.id
            }

            // reassign values to the new player
            playerPosition?.apply {
                this.order = otherPlayerPosition?.order ?: 0
                this.position = position.id

                when (position) {
                    FieldPosition.OLD_SUBSTITUTE, FieldPosition.SUBSTITUTE -> {
                        // it depends of the extra hitter size
                        val nextAvailableOrder = players.getNextAvailableOrder(listOf(this.order))
                        val substitutesBatterSize = players.filter {
                            it.isSubstitute() && it.order > 0
                        }.size
                        // there are some cases where substitutes can me inserted before defense
                        // players. We authorize only maximum extraBatterSize substitutes to be
                        // batter
                        if (nextAvailableOrder > batterSize + extraHittersSize
                            || substitutesBatterSize >= extraHittersSize
                        ) {
                            this.order = Constants.SUBSTITUTE_ORDER_VALUE
                        }
                    }
                    FieldPosition.PITCHER -> {
                        // if the new position is a pitcher for a baseball team with dh enabled, the
                        // batting order is automatically equals to 10
                        if (lineupMode == MODE_ENABLED && teamType == TeamType.BASEBALL.id) {
                            this.order = strategy.getDesignatedPlayerOrder(extraHittersSize)
                            this.flags = PlayerFieldPosition.FLAG_FLEX
                        }
                    }
                    else -> {}
                }

                // we keep the order of the previous position, except if there wasn't
                if (this.order == 0) {
                    order = players.getNextAvailableOrder()
                }

                val coordinate = position.getPositionPercentage(strategy)
                this.x = coordinate.x
                this.y = coordinate.y
            } ?: throw IllegalArgumentException("The player is not part of initial players list")

            // remove values from the old player
            otherPlayerPosition?.reset()

            Single.just(ResponseValue())
        }
    }

    class RequestValues(
        val player: Player,
        val position: FieldPosition,
        val lineup: Lineup,
        val players: List<PlayerWithPosition>,
        val teamType: Int
    ) : UseCase.RequestValues

    inner class ResponseValue : UseCase.ResponseValue
}