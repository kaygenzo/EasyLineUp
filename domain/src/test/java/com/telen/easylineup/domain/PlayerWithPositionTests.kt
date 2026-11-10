/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.isBatter
import com.telen.easylineup.domain.model.isBattingSubstitute
import com.telen.easylineup.domain.model.toPlayerFieldPosition
import com.telen.easylineup.domain.model.unassign
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class PlayerWithPositionTests {

    private fun substitute(order: Int) = PlayerWithPosition(
        playerName = "p",
        playerSex = 0,
        shirtNumber = 1,
        licenseNumber = "1",
        teamId = 1L,
        image = null,
        position = FieldPosition.SUBSTITUTE.id,
        order = order,
        playerId = 1L,
        lineupId = 1L,
        playerPositions = 0
    )

    private fun player(
        position: Int = -1,
        order: Int = 0,
        fieldPositionId: Long = 0
    ) = PlayerWithPosition(
        playerName = "p",
        playerSex = 0,
        shirtNumber = 1,
        licenseNumber = "1",
        teamId = 1L,
        image = null,
        position = position,
        order = order,
        fieldPositionId = fieldPositionId,
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

    @Test
    fun shouldBeAssignedWhenPositionIsPositive() {
        assertTrue(player(position = FieldPosition.PITCHER.id).isAssigned())
    }

    @Test
    fun shouldBeAssignedWhenOldSubstituteWithAFieldPositionId() {
        val legacySubstitute =
            player(position = FieldPosition.OLD_SUBSTITUTE.id, fieldPositionId = 1L)

        assertTrue(legacySubstitute.isAssigned())
    }

    @Test
    fun shouldNotBeAssignedAtTheDefaultUnassignedPosition() {
        assertFalse(player(position = -1).isAssigned())
    }

    @Test
    fun shouldNotBeAssignedWhenOldSubstituteWithoutAFieldPositionId() {
        val unassignedLegacySubstitute = player(position = FieldPosition.OLD_SUBSTITUTE.id)

        assertFalse(unassignedLegacySubstitute.isAssigned())
    }

    @Test
    fun shouldBeABatterWhenAssignedWithAPositiveOrder() {
        val batter = player(position = FieldPosition.PITCHER.id, order = 1)

        assertTrue(batter.isBatter())
    }

    @Test
    fun shouldNotBeABatterWhenAssignedButOrderIsZero() {
        val benchOnly = player(position = FieldPosition.PITCHER.id, order = 0)

        assertFalse(benchOnly.isBatter())
    }

    @Test
    fun shouldNotBeABatterWhenNotAssignedEvenWithAPositiveOrder() {
        val notAssigned = player(position = -1, order = 1)

        assertFalse(notAssigned.isBatter())
    }

    @Test
    fun shouldClearPositionOrderAndFlagsOnUnassign() {
        val assigned = player(position = FieldPosition.PITCHER.id, order = 3).apply {
            flags = PlayerFieldPosition.FLAG_FLEX
        }

        assigned.unassign()

        assertEquals(-1, assigned.position)
        assertEquals(0, assigned.order)
        assertEquals(PlayerFieldPosition.FLAG_NONE, assigned.flags)
    }

    @Test
    fun shouldMapToAMatchingPlayerFieldPosition() {
        val assigned = player(position = FieldPosition.PITCHER.id, order = 2, fieldPositionId = 7L)
            .apply {
                x = 12.5f
                y = 88.25f
                flags = PlayerFieldPosition.FLAG_FLEX
            }

        val fieldPosition = assigned.toPlayerFieldPosition()

        assertEquals(assigned.fieldPositionId, fieldPosition.id)
        assertEquals(assigned.playerId, fieldPosition.playerId)
        assertEquals(assigned.position, fieldPosition.position)
        assertEquals(assigned.x, fieldPosition.x)
        assertEquals(assigned.y, fieldPosition.y)
        assertEquals(assigned.order, fieldPosition.order)
        assertEquals(assigned.lineupId, fieldPosition.lineupId)
        assertEquals(assigned.flags, fieldPosition.flags)
    }
}
