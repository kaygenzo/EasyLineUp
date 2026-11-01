/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class FieldPositionTests {

    @Test
    fun shouldMatchWhenMaskBitIsSet() {
        assertTrue(FieldPosition.PITCHER.matches(FieldPosition.PITCHER.mask))
    }

    @Test
    fun shouldNotMatchWhenMaskBitIsUnset() {
        assertFalse(FieldPosition.PITCHER.matches(FieldPosition.CATCHER.mask))
    }

    @Test
    fun shouldNotMatchAnyPositionForAnEmptyMask() {
        FieldPosition.values().forEach { position ->
            assertFalse(position.matches(0))
        }
    }

    @Test
    fun shouldFindThePrimaryDefensePosition() {
        val positions = FieldPosition.PITCHER.mask or FieldPosition.CATCHER.mask

        val result = FieldPosition.findPrimaryDefensePosition(positions)

        assertEquals(FieldPosition.PITCHER, result)
    }

    @Test
    fun shouldExcludeDpDhFromThePrimaryDefensePosition() {
        val positions = FieldPosition.DP_DH.mask

        val result = FieldPosition.findPrimaryDefensePosition(positions)

        assertNull(result)
    }

    @Test
    fun shouldExcludeSubstituteFromThePrimaryDefensePosition() {
        val positions = FieldPosition.DP_DH.mask or FieldPosition.SHORT_STOP.mask

        val result = FieldPosition.findPrimaryDefensePosition(positions)

        assertEquals(FieldPosition.SHORT_STOP, result)
    }

    @Test
    fun shouldReturnNullWhenNoPositionMatches() {
        assertNull(FieldPosition.findPrimaryDefensePosition(0))
    }
}
