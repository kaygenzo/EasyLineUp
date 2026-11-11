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
import com.telen.easylineup.domain.model.getDesignatedPlayerOrder
import com.telen.easylineup.domain.model.getNextAvailableOrder
import com.telen.easylineup.domain.model.getPositionPercentage
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.model.reset
import com.telen.easylineup.domain.model.update
import com.telen.easylineup.domain.ports.DispatcherProvider
import kotlinx.coroutines.withContext

class AssignPlayerFieldPosition(
    private val getTeam: GetTeam,
    private val dispatcherProvider: DispatcherProvider
) {
    /** Returns [players] with [player] placed on [position]; [players] itself is untouched. */
    suspend operator fun invoke(
        player: Player,
        position: FieldPosition,
        lineup: Lineup,
        players: List<PlayerWithPosition>
    ): Result<List<PlayerWithPosition>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val team = getTeam().getOrThrow()
            val lineupMode = lineup.mode
            val strategy = TeamStrategy.getStrategyById(lineup.strategy)
            val batterSize = strategy.batterSize
            val extraHittersSize = lineup.extraHitters
            val teamType = team.type

            val draft = players.toMutableList()
            val otherIndex = draft.indexOfFirst {
                // another player is already on the same position
                it.position == position.id && !it.isSubstitute()
            }
            val otherOrder = draft.getOrNull(otherIndex)?.order
            val index = draft.indexOfFirst { it.playerId == player.id }

            // reassign values to the new player
            require(index >= 0) { "The player is not part of initial players list" }
            draft.update(index) { it.copy(order = otherOrder ?: 0, position = position.id) }

            when (position) {
                FieldPosition.OLD_SUBSTITUTE, FieldPosition.SUBSTITUTE -> {
                    // it depends of the extra hitter size
                    val nextAvailableOrder = draft.getNextAvailableOrder(listOf(draft[index].order))
                    val substitutesBatterSize = draft.filter {
                        it.isSubstitute() && it.order > 0
                    }.size
                    // there are some cases where substitutes can me inserted before
                    // defense players. We authorize only maximum extraBatterSize
                    // substitutes to be batter
                    if (nextAvailableOrder > batterSize + extraHittersSize
                        || substitutesBatterSize >= extraHittersSize
                    ) {
                        draft.update(index) { it.copy(order = Constants.SUBSTITUTE_ORDER_VALUE) }
                    }
                }

                FieldPosition.PITCHER -> {
                    // if the new position is a pitcher for a baseball team with dh
                    // enabled, the batting order is automatically equals to 10
                    if (lineupMode == MODE_ENABLED && teamType == TeamType.BASEBALL.id) {
                        draft.update(index) {
                            it.copy(
                                order = strategy.getDesignatedPlayerOrder(extraHittersSize),
                                flags = PlayerFieldPosition.FLAG_FLEX
                            )
                        }
                    } else {
                        draft.update(index) { it.copy(flags = PlayerFieldPosition.FLAG_NONE) }
                    }
                }

                else -> {
                    // clear a leftover flex flag if this player was previously the
                    // DH/flex and is now being placed on a regular position through this
                    // direct assignment path (SwitchPlayersPosition already does this,
                    // this use case did not).
                    draft.update(index) { it.copy(flags = PlayerFieldPosition.FLAG_NONE) }
                }
            }

            // we keep the order of the previous position, except if there wasn't
            if (draft[index].order == 0) {
                val order = draft.getNextAvailableOrder()
                draft.update(index) { it.copy(order = order) }
            }

            val coordinate = position.getPositionPercentage(strategy)
            draft.update(index) { it.copy(x = coordinate.x, y = coordinate.y) }

            // remove values from the old player
            if (otherIndex >= 0) {
                draft.update(otherIndex) { it.reset() }
            }
            draft
        }
    }
}
