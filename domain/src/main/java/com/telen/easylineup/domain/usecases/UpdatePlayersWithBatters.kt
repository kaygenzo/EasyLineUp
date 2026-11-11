/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.ports.DispatcherProvider
import kotlinx.coroutines.withContext

class UpdatePlayersWithBatters(private val dispatcherProvider: DispatcherProvider) {
    /** Returns [players] in the batting order of [batters]; [players] itself is untouched. */
    suspend operator fun invoke(
        players: List<PlayerWithPosition>,
        batters: List<BatterState>
    ): Result<List<PlayerWithPosition>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            // As when each batter updated the list in turn: the last batter of a player wins,
            // and only that player's first entry in the list is changed.
            val orders = batters.associate { it.playerId to it.playerOrder }
            val firstEntries = players.withIndex()
                .distinctBy { it.value.playerId }
                .map { it.index }
                .toSet()
            players.mapIndexed { index, player ->
                val order = orders[player.playerId]
                if (order != null && index in firstEntries) player.copy(order = order) else player
            }
        }
    }
}
