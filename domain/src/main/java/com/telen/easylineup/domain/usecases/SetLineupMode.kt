/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.ports.DispatcherProvider
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class SetLineupMode(
    private val getTeam: GetTeam,
    private val updatePlayersWithLineupMode: UpdatePlayersWithLineupMode,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(
        isEnabled: Boolean,
        lineup: Lineup,
        players: List<PlayerWithPosition>
    ): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            lineup.mode = if (isEnabled) MODE_ENABLED else MODE_DISABLED
            val team = getTeam().await()
            updatePlayersWithLineupMode(players, lineup, team.type).getOrThrow()
        }
    }
}
