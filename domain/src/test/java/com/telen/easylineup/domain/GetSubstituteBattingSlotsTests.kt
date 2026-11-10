/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.SubstituteBattingSlots
import com.telen.easylineup.domain.usecases.GetSubstituteBattingSlots
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class GetSubstituteBattingSlotsTests {
    private lateinit var getSubstituteBattingSlots: GetSubstituteBattingSlots

    private fun battingSubstitute(id: Long, order: Int) = PlayerWithPosition(
        playerName = "p$id",
        playerSex = 0,
        shirtNumber = id.toInt(),
        licenseNumber = id.toString(),
        teamId = 1L,
        image = null,
        position = FieldPosition.SUBSTITUTE.id,
        order = order,
        playerId = id,
        lineupId = 1L,
        playerPositions = 0
    )

    @Before
    fun init() {
        getSubstituteBattingSlots = GetSubstituteBattingSlots()
    }

    @Test
    fun shouldBeUnlimitedBeyondTheConfiguredThreshold() {
        val result = getSubstituteBattingSlots(
            emptyList(),
            Constants.MAX_EXTRA_HITTERS_BEFORE_UNLIMITED + 1
        )

        assertEquals(SubstituteBattingSlots.Unlimited, result)
    }

    @Test
    fun shouldCountFreeSlotsFromBattingSubstitutesOnly() {
        val players = listOf(battingSubstitute(1L, order = 1), battingSubstitute(2L, order = 2))

        val result = getSubstituteBattingSlots(players, extraHitters = 3)

        assertEquals(SubstituteBattingSlots.Limited(remaining = 1, total = 3), result)
    }

    @Test
    fun shouldIgnoreBenchedSubstitutesWithNoOrder() {
        val players = listOf(battingSubstitute(1L, order = 0))

        val result = getSubstituteBattingSlots(players, extraHitters = 1)

        assertEquals(SubstituteBattingSlots.Limited(remaining = 1, total = 1), result)
    }

    @Test
    fun shouldNotGoBelowZeroRemaining() {
        val players = listOf(
            battingSubstitute(1L, order = 1),
            battingSubstitute(2L, order = 2),
            battingSubstitute(3L, order = 3)
        )

        val result = getSubstituteBattingSlots(players, extraHitters = 1)

        assertEquals(SubstituteBattingSlots.Limited(remaining = 0, total = 1), result)
    }
}
