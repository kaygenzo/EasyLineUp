/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.usecases.HasUnsavedLineupEdits
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class HasUnsavedLineupEditsTests {
    private val hasUnsavedLineupEdits = HasUnsavedLineupEdits()
    private val lineup = Lineup(id = 3L, name = "Game 1", tournamentId = 7L)
    private val roster = TeamRosterSummary(
        Constants.STATUS_ALL,
        listOf(RosterPlayerStatus(Player(1L, 1L, "Ana", 4, "L1"), true, null))
    )

    @Test
    fun nothingChangedIsNothingToLose() {
        assertFalse(hasUnsavedLineupEdits(lineup, roster, "Game 1", 7L, roster))
    }

    @Test
    fun aNewNameIsAChange() {
        assertTrue(hasUnsavedLineupEdits(lineup, roster, "Game 2", 7L, roster))
    }

    @Test
    fun anotherTournamentIsAChange() {
        assertTrue(hasUnsavedLineupEdits(lineup, roster, "Game 1", 8L, roster))
    }

    @Test
    fun aTournamentThatCouldNotBeFoundIsAChange() {
        assertTrue(hasUnsavedLineupEdits(lineup, roster, "Game 1", null, roster))
    }

    @Test
    fun aPlayerLeftOutIsAChange() {
        val edited = roster.copy(
            status = Constants.STATUS_PARTIAL,
            players = roster.players.map { it.copy(status = false) }
        )

        assertTrue(hasUnsavedLineupEdits(lineup, roster, "Game 1", 7L, edited))
    }
}
