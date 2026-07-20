/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.ports.DispatcherProvider
import kotlinx.coroutines.withContext

class UpdatePlayersWithBatters(private val dispatcherProvider: DispatcherProvider) {
    suspend operator fun invoke(
        players: List<PlayerWithPosition>,
        batters: List<BatterState>
    ): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            batters.forEach { apply(players, it) }
        }
    }

    private fun apply(players: List<PlayerWithPosition>, batter: BatterState) {
        players
            .firstOrNull { it.playerId == batter.playerId }
            ?.let { it.order = batter.playerOrder }
    }
}
