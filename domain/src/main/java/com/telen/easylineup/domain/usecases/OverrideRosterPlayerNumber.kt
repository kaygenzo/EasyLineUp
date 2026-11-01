/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.TeamRosterSummary

/** Sets (creating if needed) the lineup-specific shirt number override for one roster player. */
class OverrideRosterPlayerNumber {
    operator fun invoke(
        roster: TeamRosterSummary,
        position: Int,
        number: Int,
        lineupId: Long
    ): TeamRosterSummary {
        val updatedPlayers = roster.players.mapIndexed { index, rosterPlayer ->
            if (index == position) {
                val overlay = rosterPlayer.playerNumberOverlay?.copy(number = number)
                    ?: PlayerNumberOverlay(
                        lineupId = lineupId,
                        playerId = rosterPlayer.player.id,
                        number = number
                    )
                rosterPlayer.copy(playerNumberOverlay = overlay)
            } else {
                rosterPlayer
            }
        }
        return roster.copy(players = updatedPlayers)
    }
}
