/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isBattingSubstitute
import com.telen.easylineup.domain.model.isDpDhOrFlex
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.model.reset
import com.telen.easylineup.domain.model.unassign
import com.telen.easylineup.domain.model.update
import com.telen.easylineup.domain.ports.DispatcherProvider
import kotlinx.coroutines.withContext

class DeletePlayerFieldPosition(private val dispatcherProvider: DispatcherProvider) {
    /** Returns [players] without [playerToDelete] on the field; [players] itself is untouched. */
    suspend operator fun invoke(
        players: List<PlayerWithPosition>,
        playerToDelete: Player,
        lineupMode: Int,
        extraHitterSize: Int
    ): Result<List<PlayerWithPosition>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val draft = players.toMutableList()
            // substitutes have the same position, let's use player to get the good one
            val index = draft.indexOfFirst { it.playerId == playerToDelete.id }
            if (index < 0) {
                throw NoSuchElementException("The player is not part of the lineup")
            }
            val player = draft[index]

            val playerIsSubstitute = player.isSubstitute()
            val playerOrder = player.order
            if (lineupMode == MODE_ENABLED && player.isDpDhOrFlex()) {
                draft.indices
                    .filter { draft[it].isDpDhOrFlex() }
                    .forEach { draft.update(it) { p -> p.reset() } }
            } else if (playerIsSubstitute) {
                // already on the bench - "deleting" them removes them from the lineup
                // entirely, rather than resetting them back to the same substitute state
                draft.update(index) { it.unassign() }
            } else {
                // Back to the bench, but as a batter if an extra-hitter slot is still free,
                // keeping their spot in the order - the same "will bat"/"bench only" rule as
                // adding a substitute. A plain reset() left order 0, which matches neither and
                // always showed as bench-only.
                val previousOrder = player.order
                val keepsBatting = previousOrder in 1 until Constants.SUBSTITUTE_ORDER_VALUE &&
                    draft.count { it.isBattingSubstitute() } < extraHitterSize
                val order = if (keepsBatting) previousOrder else Constants.SUBSTITUTE_ORDER_VALUE
                draft.update(index) { it.reset().copy(order = order) }
            }

            // check if substitutes was a batter, update the next substitutes order to replace
            // it if necessary
            if (playerIsSubstitute && playerOrder < Constants.SUBSTITUTE_ORDER_VALUE) {
                val substitutes = draft.indices.filter {
                    draft[it].isSubstitute() && draft[it].order > 0
                }
                var found = false
                substitutes.forEachIndexed { rank, substituteIndex ->
                    if (!found && rank < extraHitterSize
                        && draft[substituteIndex].order == Constants.SUBSTITUTE_ORDER_VALUE
                    ) {
                        draft.update(substituteIndex) { it.copy(order = playerOrder) }
                        found = true
                    }
                }
            }
            draft
        }
    }
}
