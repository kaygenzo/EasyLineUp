/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.LineupDraft
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.toPlayer

/**
 * Applies an edited roster to a lineup in memory, without writing anything: every player
 * taken off the roster leaves [players] (field players first go back to the bench, then leave
 * it, exactly like removing them twice by hand), each remaining player shows the shirt number
 * the roster gives them for this lineup, and the returned lineup carries the new roster.
 *
 * [players] is mutated in place, the way the lineup editor's draft is. Saving is left to the
 * caller - either straight away ([UpdateLineupRoster]) or along with the rest of an edit.
 */
class ApplyRosterToLineup(private val deletePlayerFieldPosition: DeletePlayerFieldPosition) {
    /**
     * Returns [lineup] with [roster] as its roster, and [players] with every player left out of
     * it taken off the field and every per-lineup shirt number applied. [players] is untouched.
     */
    suspend operator fun invoke(
        lineup: Lineup,
        players: List<PlayerWithPosition>,
        roster: List<RosterPlayerStatus>
    ): Result<LineupDraft> = runCatchingCancellable {
        val removedIds = roster.filterNot { it.status }.map { it.player.id }.toSet()
        var draft = players
        players.filter { it.playerId in removedIds && it.isAssigned() }.forEach { removed ->
            repeat(2) {
                val player = draft.first { it.playerId == removed.playerId }
                if (!player.isAssigned()) return@repeat
                draft = deletePlayerFieldPosition(
                    draft,
                    player.toPlayer(),
                    lineup.mode,
                    lineup.extraHitters
                ).getOrThrow()
            }
        }

        val shirtNumbers = roster.associate { status ->
            status.player.id to (status.playerNumberOverlay?.number ?: status.player.shirtNumber)
        }
        draft = draft.map { player ->
            shirtNumbers[player.playerId]?.let { player.copy(shirtNumber = it) } ?: player
        }

        LineupDraft(lineup.copy(roster = rosterToString(roster)), draft)
    }

    private fun rosterToString(list: List<RosterPlayerStatus>): String {
        return list.filter { it.status }.joinToString(";") { it.player.id.toString() }
    }
}
