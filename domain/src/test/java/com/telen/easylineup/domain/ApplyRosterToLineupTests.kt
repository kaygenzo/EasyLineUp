/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.usecases.ApplyRosterToLineup
import com.telen.easylineup.domain.usecases.DeletePlayerFieldPosition
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ApplyRosterToLineupTests {
    private val applyRosterToLineup =
        ApplyRosterToLineup(DeletePlayerFieldPosition(testDispatcherProvider()))
    private val lineup = Lineup(id = 1L, name = "Game 1", teamId = 1L, tournamentId = 1L)

    private fun player(id: Long) = Player(
        id = id,
        teamId = 1L,
        name = "p$id",
        shirtNumber = id.toInt(),
        licenseNumber = id.toString()
    )

    private fun positioned(id: Long, position: FieldPosition, order: Int) = PlayerWithPosition(
        playerName = "p$id",
        playerSex = 0,
        shirtNumber = id.toInt(),
        licenseNumber = id.toString(),
        teamId = 1L,
        image = null,
        position = position.id,
        order = order,
        fieldPositionId = id * 10,
        playerId = id,
        lineupId = 1L,
        playerPositions = 0
    )

    private fun status(id: Long, selected: Boolean, number: Int? = null) = RosterPlayerStatus(
        player(id),
        status = selected,
        playerNumberOverlay = number?.let {
            PlayerNumberOverlay(lineupId = 1L, playerId = id, number = it)
        }
    )

    @Test
    fun shouldTakeAFieldPlayerLeftOffTheRosterOutOfTheLineup() = runTest {
        val pitcher = positioned(1L, FieldPosition.PITCHER, 1)
        val catcher = positioned(2L, FieldPosition.CATCHER, 2)

        val result = applyRosterToLineup(
            lineup,
            listOf(pitcher, catcher),
            listOf(status(1L, true), status(2L, false))
        )

        assertTrue(result.isSuccess)
        assertTrue(pitcher.isAssigned())
        assertFalse(catcher.isAssigned())
    }

    @Test
    fun shouldKeepEveryoneStillOnTheRoster() = runTest {
        val pitcher = positioned(1L, FieldPosition.PITCHER, 1)

        applyRosterToLineup(lineup, listOf(pitcher), listOf(status(1L, true)))

        assertEquals(FieldPosition.PITCHER.id, pitcher.position)
        assertEquals(1, pitcher.order)
    }

    @Test
    fun shouldReturnTheLineupWithTheNewRoster() = runTest {
        val result = applyRosterToLineup(
            lineup,
            emptyList(),
            listOf(status(1L, true), status(2L, false), status(3L, true))
        )

        assertEquals("1;3", result.getOrThrow().roster)
    }

    @Test
    fun shouldShowTheShirtNumberGivenForThisLineup() = runTest {
        val pitcher = positioned(1L, FieldPosition.PITCHER, 1)
        val catcher = positioned(2L, FieldPosition.CATCHER, 2)

        applyRosterToLineup(
            lineup,
            listOf(pitcher, catcher),
            listOf(status(1L, true, number = 42), status(2L, true))
        )

        assertEquals(42, pitcher.shirtNumber)
        assertEquals(2, catcher.shirtNumber)
    }

    @Test
    fun shouldNotWriteAnything() = runTest {
        // No repository at all: the use case can only work on what it was given.
        val pitcher = positioned(1L, FieldPosition.PITCHER, 1)

        val result = applyRosterToLineup(lineup, listOf(pitcher), listOf(status(1L, false)))

        assertTrue(result.isSuccess)
        assertFalse(pitcher.isAssigned())
    }
}
