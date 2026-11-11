/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.usecases.OverrideRosterPlayerNumber
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class OverrideRosterPlayerNumberTests {
    private lateinit var overrideRosterPlayerNumber: OverrideRosterPlayerNumber

    private fun player(id: Long) =
        Player(
            id = id,
            teamId = 1L,
            name = "p$id",
            shirtNumber = id.toInt(),
            licenseNumber = id.toString()
        )

    @Before
    fun init() {
        overrideRosterPlayerNumber = OverrideRosterPlayerNumber()
    }

    @Test
    fun shouldCreateOverlayWhenPlayerHasNone() {
        val roster = TeamRosterSummary(
            status = Constants.STATUS_ALL,
            players = listOf(RosterPlayerStatus(
                player(1L),
                status = true,
                playerNumberOverlay = null
            ))
        )

        val result = overrideRosterPlayerNumber(roster, position = 0, number = 42, lineupId = 7L)

        val overlay = result.players[0].playerNumberOverlay
        assertEquals(42, overlay?.number)
        assertEquals(7L, overlay?.lineupId)
        assertEquals(1L, overlay?.playerId)
    }

    @Test
    fun shouldUpdateExistingOverlayNumber() {
        val existing = PlayerNumberOverlay(lineupId = 7L, playerId = 1L, number = 9)
        val roster = TeamRosterSummary(
            status = Constants.STATUS_ALL,
            players = listOf(
                RosterPlayerStatus(player(1L), status = true, playerNumberOverlay = existing)
            )
        )

        val result = overrideRosterPlayerNumber(roster, position = 0, number = 42, lineupId = 7L)

        assertEquals(42, result.players[0].playerNumberOverlay?.number)
    }

    @Test
    fun shouldLeaveOtherPlayersUntouched() {
        val roster = TeamRosterSummary(
            status = Constants.STATUS_ALL,
            players = listOf(
                RosterPlayerStatus(player(1L), status = true, playerNumberOverlay = null),
                RosterPlayerStatus(player(2L), status = true, playerNumberOverlay = null)
            )
        )

        val result = overrideRosterPlayerNumber(roster, position = 0, number = 42, lineupId = 7L)

        assertEquals(null, result.players[1].playerNumberOverlay)
    }
}
