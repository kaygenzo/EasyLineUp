/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.usecases.IsPlayingTimeUnderused
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class IsPlayingTimeUnderusedTests {
    private lateinit var isPlayingTimeUnderused: IsPlayingTimeUnderused

    @Before
    fun init() {
        isPlayingTimeUnderused = IsPlayingTimeUnderused()
    }

    @Test
    fun shouldFlagAPlayerBelowHalfTheAverage() {
        assertTrue(isPlayingTimeUnderused(totalGames = 2, average = 10f))
    }

    @Test
    fun shouldNotFlagAPlayerAtOrAboveHalfTheAverage() {
        assertFalse(isPlayingTimeUnderused(totalGames = 5, average = 10f))
    }

    @Test
    fun shouldNotFlagAnyoneWhenAverageIsZero() {
        assertFalse(isPlayingTimeUnderused(totalGames = 0, average = 0f))
    }
}
