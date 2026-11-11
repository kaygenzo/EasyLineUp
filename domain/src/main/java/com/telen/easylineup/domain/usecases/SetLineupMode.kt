/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.LineupDraft
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.ports.DispatcherProvider
import kotlinx.coroutines.withContext

class SetLineupMode(
    private val getTeam: GetTeam,
    private val updatePlayersWithLineupMode: UpdatePlayersWithLineupMode,
    private val dispatcherProvider: DispatcherProvider
) {
    /** Returns [lineup] in its new mode and [players] adjusted to it. [players] is untouched. */
    suspend operator fun invoke(
        isEnabled: Boolean,
        lineup: Lineup,
        players: List<PlayerWithPosition>
    ): Result<LineupDraft> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val updatedLineup = lineup.copy(mode = if (isEnabled) MODE_ENABLED else MODE_DISABLED)
            val team = getTeam().getOrThrow()
            val updatedPlayers =
                updatePlayersWithLineupMode(players, updatedLineup, team.type).getOrThrow()
            LineupDraft(updatedLineup, updatedPlayers)
        }
    }
}
