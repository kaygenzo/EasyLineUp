/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.ResolveLineupEventTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class ResolveLineupEventTimeTests {
    private lateinit var resolveLineupEventTime: ResolveLineupEventTime

    @Before
    fun init() {
        resolveLineupEventTime = ResolveLineupEventTime()
    }

    @Test
    fun shouldKeepCurrentTimeWhenInsideTournamentWindow() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 100L, endTime = 200L)

        val result = resolveLineupEventTime(tournament, 150L)

        assertEquals(150L, result)
    }

    @Test
    fun shouldSnapToStartTimeWhenCurrentTimeIsBeforeWindow() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 100L, endTime = 200L)

        val result = resolveLineupEventTime(tournament, 50L)

        assertEquals(100L, result)
    }

    @Test
    fun shouldSnapToStartTimeWhenCurrentTimeIsAfterWindow() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 100L, endTime = 200L)

        val result = resolveLineupEventTime(tournament, 250L)

        assertEquals(100L, result)
    }

    @Test
    fun shouldReturnNowWhenTournamentHasNoWindow() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 0L, endTime = 0L)

        val before = System.currentTimeMillis()
        val result = resolveLineupEventTime(tournament, 150L)

        assertTrue(result >= before)
    }
}
