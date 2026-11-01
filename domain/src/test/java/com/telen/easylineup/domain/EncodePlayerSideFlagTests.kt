/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.usecases.EncodePlayerSideFlag
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class EncodePlayerSideFlagTests {
    private lateinit var encodePlayerSideFlag: EncodePlayerSideFlag

    @Before
    fun init() {
        encodePlayerSideFlag = EncodePlayerSideFlag()
    }

    @Test
    fun shouldReturnZeroWhenNeitherSideIsSelected() {
        assertEquals(0, encodePlayerSideFlag(left = false, right = false))
    }

    @Test
    fun shouldReturnLeftFlagWhenOnlyLeftIsSelected() {
        assertEquals(PlayerSide.LEFT.flag, encodePlayerSideFlag(left = true, right = false))
    }

    @Test
    fun shouldReturnRightFlagWhenOnlyRightIsSelected() {
        assertEquals(PlayerSide.RIGHT.flag, encodePlayerSideFlag(left = false, right = true))
    }

    @Test
    fun shouldReturnBothFlagWhenBothSidesAreSelected() {
        assertEquals(PlayerSide.BOTH.flag, encodePlayerSideFlag(left = true, right = true))
    }
}
