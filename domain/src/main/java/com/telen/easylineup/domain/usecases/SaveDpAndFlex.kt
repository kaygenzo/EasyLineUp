/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.getDesignatedPlayerOrder
import com.telen.easylineup.domain.model.getNextAvailableOrder
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isFlex
import com.telen.easylineup.domain.model.reset
import com.telen.easylineup.domain.model.update
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import kotlinx.coroutines.withContext

class SaveDpAndFlex(private val dispatcherProvider: DispatcherProvider) {
    /** Returns [players] with [dp] and [flex] linked; [players] itself is untouched. */
    suspend operator fun invoke(
        lineup: Lineup,
        dp: Player?,
        flex: Player?,
        players: List<PlayerWithPosition>
    ): Result<List<PlayerWithPosition>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val theDp = dp ?: throw NeedAssignBothPlayersException()
            val theFlex = flex ?: throw NeedAssignBothPlayersException()
            val strategy = TeamStrategy.getStrategyById(lineup.strategy)
            val draft = players.toMutableList()

            val oldFlex = draft.indices.filter { draft[it].isFlex() }

            // assign the new player as flex
            val newFlex = draft.indexOfFirst { it.playerId == theFlex.id }
            if (newFlex >= 0) {
                draft.update(newFlex) {
                    it.copy(
                        flags = PlayerFieldPosition.FLAG_FLEX,
                        order = strategy.getDesignatedPlayerOrder(lineup.extraHitters)
                    )
                }
            }

            // reset old flex flags and reset them because there can be only one flex
            oldFlex.filter { draft[it].playerId != theFlex.id }.forEach { index ->
                draft.update(index) { it.copy(flags = PlayerFieldPosition.FLAG_NONE) }
                val order = draft.getNextAvailableOrder(listOf(draft[index].order))
                draft.update(index) { it.copy(order = order) }
            }

            // check if there is a dp and reset him because there can be only one dp
            val oldDp = draft.indexOfFirst { it.isDpDh() }
            if (oldDp >= 0) {
                draft.update(oldDp) { it.reset() }
            }

            val newDp = draft.indexOfFirst { it.playerId == theDp.id }
            if (newDp >= 0) {
                draft.update(newDp) { it.copy(position = FieldPosition.DP_DH.id) }
                val order = draft.getNextAvailableOrder(listOf(draft[newDp].order))
                draft.update(newDp) { it.copy(order = order) }
            }
            draft
        }
    }
}
