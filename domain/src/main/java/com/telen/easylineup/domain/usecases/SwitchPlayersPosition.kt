/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.getDesignatedPlayerOrder
import com.telen.easylineup.domain.model.getNextAvailableOrder
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isFlex
import com.telen.easylineup.domain.model.update
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.usecases.exceptions.FirstPositionEmptyException
import com.telen.easylineup.domain.usecases.exceptions.SamePlayerException
import kotlinx.coroutines.withContext

class SwitchPlayersPosition(
    private val getTeam: GetTeam,
    private val dispatcherProvider: DispatcherProvider
) {
    /** Returns [players] with both positions swapped; [players] itself is untouched. */
    suspend operator fun invoke(
        players: List<PlayerWithPosition>,
        position1: FieldPosition,
        position2: FieldPosition,
        lineup: Lineup
    ): Result<List<PlayerWithPosition>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val team = getTeam().getOrThrow()
            val draft = players.toMutableList()
            val extraHittersSize = lineup.extraHitters
            val lineupMode = lineup.mode
            val strategy = TeamStrategy.getStrategyById(lineup.strategy)
            val teamType = team.type

            val index1 = draft.indexOfFirst { it.position == position1.id }
            if (index1 < 0) {
                throw FirstPositionEmptyException()
            }
            val index2 = draft.indexOfFirst { it.position == position2.id }.takeIf { it >= 0 }

            if (index1 == index2) {
                throw SamePlayerException()
            }

            draft.update(index1) { it.copy(position = position2.id) }
            index2?.let { draft.update(it) { player -> player.copy(position = position1.id) } }

            val switched = listOfNotNull(index1, index2)

            val orderDesignatedPlayer =
                strategy.getDesignatedPlayerOrder(extraHittersSize)

            // just keep reference of the first order different of 10
            val tmpOrder = switched.map { draft[it] }
                .firstOrNull { it.order != orderDesignatedPlayer }?.order
            val oneIsFlex = switched.any { draft[it].isFlex() }
            val oneIsDp = switched.any { draft[it].isDpDh() }

            // if at least one player has a flag flex and it is a baseball team, the pitcher is
            // always the flex. Otherwise, the flex can be any other player. The only exception is
            // the DP/DH
            switched.forEach { index ->
                if (lineupMode == MODE_ENABLED) {
                    val newPosition = FieldPosition.getFieldPositionById(draft[index].position)
                    if (teamType == TeamType.BASEBALL.id) {
                        when (newPosition) {
                            FieldPosition.PITCHER -> draft.update(index) {
                                it.copy(
                                    order = orderDesignatedPlayer,
                                    flags = PlayerFieldPosition.FLAG_FLEX
                                )
                            }

                            else -> {
                                draft.clearFlags(index)
                                // if possible, just keep order, but in case of a swap with a
                                // pitcher, exchange orders
                                draft.leaveDesignatedOrder(index, orderDesignatedPlayer, tmpOrder)
                            }
                        }
                    } else {
                        // softball
                        /*
                        * Rules:
                        * - Flex can be anyone except DP.
                        * - If FLEX exists, its order is 10
                        * - If 2 players switch their positions:
                        *   if it is 2 normal players, only positions change
                        *   if it is a FLEX and a normal player, only positions change
                        *   if it is the DP and a normal player, only positions change
                        *   if is is the DP and the FLEX, switch position, flags and order
                        */
                        if (oneIsFlex && oneIsDp) {
                            if (newPosition == FieldPosition.DP_DH) {
                                draft.clearFlags(index)
                                draft.leaveDesignatedOrder(index, orderDesignatedPlayer, tmpOrder)
                            } else {
                                draft.update(index) {
                                    it.copy(
                                        order = orderDesignatedPlayer,
                                        flags = PlayerFieldPosition.FLAG_FLEX
                                    )
                                }
                            }
                        }
                    }
                }
            }
            draft
        }
    }

    private fun MutableList<PlayerWithPosition>.clearFlags(index: Int) {
        update(index) { it.copy(flags = PlayerFieldPosition.FLAG_NONE) }
    }

    /** A player who held the designated player's order, but no longer plays that role. */
    private fun MutableList<PlayerWithPosition>.leaveDesignatedOrder(
        index: Int,
        orderDesignatedPlayer: Int,
        tmpOrder: Int?
    ) {
        val order = this[index].order
        if (order == orderDesignatedPlayer) {
            // we were a pitcher but not anymore.
            val newOrder = tmpOrder ?: getNextAvailableOrder(listOf(order))
            update(index) { it.copy(order = newOrder) }
        }
    }
}
