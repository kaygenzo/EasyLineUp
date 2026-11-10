/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.usecases.ClearRoster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class ClearRosterTests {
    private lateinit var clearRoster: ClearRoster

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
        clearRoster = ClearRoster()
    }

    @Test
    fun shouldDeselectEveryPlayer() {
        val roster = TeamRosterSummary(
            status = Constants.STATUS_ALL,
            players = listOf(
                RosterPlayerStatus(player(1L), status = true, playerNumberOverlay = null),
                RosterPlayerStatus(player(2L), status = true, playerNumberOverlay = null)
            )
        )

        val result = clearRoster(roster)

        assertTrue(result.players.none { it.status })
    }

    @Test
    fun shouldReportPartialStatus() {
        val roster = TeamRosterSummary(
            status = Constants.STATUS_ALL,
            players = listOf(RosterPlayerStatus(player(1L), status = true, playerNumberOverlay = null))
        )

        val result = clearRoster(roster)

        assertEquals(Constants.STATUS_PARTIAL, result.status)
    }
}
