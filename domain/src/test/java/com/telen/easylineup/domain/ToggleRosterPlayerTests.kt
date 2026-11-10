/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.usecases.ToggleRosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class ToggleRosterPlayerTests {
    private lateinit var toggleRosterPlayer: ToggleRosterPlayer

    private fun player(id: Long) =
        Player(
            id = id,
            teamId = 1L,
            name = "p$id",
            shirtNumber = id.toInt(),
            licenseNumber = id.toString()
        )

    private fun roster(vararg selected: Boolean) = TeamRosterSummary(
        status = Constants.STATUS_ALL,
        players = selected.mapIndexed { index, status ->
            RosterPlayerStatus(player(index.toLong()), status, null)
        }
    )

    @Before
    fun init() {
        toggleRosterPlayer = ToggleRosterPlayer()
    }

    @Test
    fun shouldDeselectPlayerAtPosition() {
        val result = toggleRosterPlayer(roster(true, true), position = 0, selected = false)

        assertFalse(result.players[0].status)
        assertTrue(result.players[1].status)
    }

    @Test
    fun shouldReportAllWhenEveryPlayerEndsUpSelected() {
        val result = toggleRosterPlayer(roster(true, false), position = 1, selected = true)

        assertEquals(Constants.STATUS_ALL, result.status)
    }

    @Test
    fun shouldReportPartialWhenNotEveryPlayerIsSelected() {
        val result = toggleRosterPlayer(roster(true, true), position = 0, selected = false)

        assertEquals(Constants.STATUS_PARTIAL, result.status)
    }
}
