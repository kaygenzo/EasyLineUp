/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.TeamRosterSummary

/**
 * Toggles one roster player's selection and recomputes the roster-wide status:
 * [Constants.STATUS_ALL] when every player ends up selected, [Constants.STATUS_PARTIAL]
 * otherwise.
 */
class ToggleRosterPlayer {
    operator fun invoke(
        roster: TeamRosterSummary,
        position: Int,
        selected: Boolean
    ): TeamRosterSummary {
        val updatedPlayers = roster.players.mapIndexed { index, player ->
            if (index == position) player.copy(status = selected) else player
        }
        val allSelected = updatedPlayers.all { it.status }
        return TeamRosterSummary(
            status = if (allSelected) Constants.STATUS_ALL else Constants.STATUS_PARTIAL,
            players = updatedPlayers
        )
    }
}
