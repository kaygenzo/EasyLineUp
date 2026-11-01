/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isBattingSubstitute
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class PlayerWithPositionTests {

    private fun substitute(order: Int) = PlayerWithPosition(
        playerName = "p",
        playerSex = 0,
        shirtNumber = 1,
        licenseNumber = 1L,
        teamId = 1L,
        image = null,
        position = FieldPosition.SUBSTITUTE.id,
        order = order,
        playerId = 1L,
        lineupId = 1L,
        playerPositions = 0
    )

    @Test
    fun shouldBeBattingWhenOrderIsWithinTheBattingRange() {
        assertTrue(substitute(order = 1).isBattingSubstitute())
    }

    @Test
    fun shouldNotBeBattingWhenOrderIsZero() {
        assertFalse(substitute(order = 0).isBattingSubstitute())
    }

    @Test
    fun shouldNotBeBattingAtTheBenchOnlySentinelOrder() {
        assertFalse(substitute(order = Constants.SUBSTITUTE_ORDER_VALUE).isBattingSubstitute())
    }

    @Test
    fun shouldNotBeBattingWhenNotASubstitute() {
        val fielder = substitute(order = 1).copy(position = FieldPosition.PITCHER.id)

        assertFalse(fielder.isBattingSubstitute())
    }
}
