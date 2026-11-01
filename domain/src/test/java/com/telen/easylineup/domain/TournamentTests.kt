/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.hasDateWindow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class TournamentTests {

    @Test
    fun shouldHaveADateWindowWhenBothTimesAreSet() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 100L, endTime = 200L)

        assertTrue(tournament.hasDateWindow())
    }

    @Test
    fun shouldNotHaveADateWindowWhenStartTimeIsMissing() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 0L, endTime = 200L)

        assertFalse(tournament.hasDateWindow())
    }

    @Test
    fun shouldNotHaveADateWindowWhenEndTimeIsMissing() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 100L, endTime = 0L)

        assertFalse(tournament.hasDateWindow())
    }
}
