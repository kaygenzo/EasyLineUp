/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.usecases.DecodePlayerSideFlag
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class DecodePlayerSideFlagTests {
    private lateinit var decodePlayerSideFlag: DecodePlayerSideFlag

    @Before
    fun init() {
        decodePlayerSideFlag = DecodePlayerSideFlag()
    }

    @Test
    fun shouldDecodeNoFlagToNeitherSide() {
        val result = decodePlayerSideFlag(0)

        assertFalse(result.left)
        assertFalse(result.right)
    }

    @Test
    fun shouldDecodeLeftFlag() {
        val result = decodePlayerSideFlag(PlayerSide.LEFT.flag)

        assertTrue(result.left)
        assertFalse(result.right)
    }

    @Test
    fun shouldDecodeBothFlag() {
        val result = decodePlayerSideFlag(PlayerSide.BOTH.flag)

        assertTrue(result.left)
        assertTrue(result.right)
    }
}
