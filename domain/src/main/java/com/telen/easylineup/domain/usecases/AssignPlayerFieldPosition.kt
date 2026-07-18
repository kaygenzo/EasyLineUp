/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.getNextAvailableOrder
import com.telen.easylineup.domain.model.getPositionPercentage
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.model.reset
import io.reactivex.rxjava3.core.Completable

class AssignPlayerFieldPosition(
    private val getTeam: GetTeam,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(
        player: Player,
        position: FieldPosition,
        lineup: Lineup,
        players: List<PlayerWithPosition>
    ): Completable {
        return getTeam().flatMapCompletable { team ->
            val lineupMode = lineup.mode
            val strategy = TeamStrategy.getStrategyById(lineup.strategy)
            val batterSize = strategy.batterSize
            val extraHittersSize = lineup.extraHitters
            val teamType = team.type

            val otherPlayerPosition = players.firstOrNull {
                // another player is already on the same position
                it.position == position.id && !it.isSubstitute()
            }
            val playerPosition = players.firstOrNull {
                it.playerId == player.id
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

            Completable.complete()
        }.subscribeOn(schedulersProvider.io())
    }
}
