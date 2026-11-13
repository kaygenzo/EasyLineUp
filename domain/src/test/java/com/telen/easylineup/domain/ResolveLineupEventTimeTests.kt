/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.ResolveLineupEventTime
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class ResolveLineupEventTimeTests {
    private lateinit var resolveLineupEventTime: ResolveLineupEventTime

    @Before
    fun init() {
        resolveLineupEventTime = ResolveLineupEventTime(fixedClock())
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
    fun shouldSnapToStartTimeWhenCurrentTimeIsAfterTheLastDay() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 100L, endTime = 200L)

        val result = resolveLineupEventTime(tournament, 200L + DAY)

        assertEquals(100L, result)
    }

    @Test
    fun shouldKeepATimeDuringTheLastDayOfTheTournament() {
        val firstDay = midnight(2026, Calendar.JUNE, 12)
        val lastDay = midnight(2026, Calendar.JUNE, 14)
        val tournament =
            Tournament(name = "t", createdAt = 0, startTime = firstDay, endTime = lastDay)
        val lastDayAfternoon = lastDay + 15 * HOUR

        assertEquals(lastDayAfternoon, resolve(tournament, lastDayAfternoon))
        assertEquals(firstDay, resolve(tournament, midnight(2026, Calendar.JUNE, 15)))
    }

    @Test
    fun shouldCountAWholeLastDayWhenTheClocksChange() {
        // Clocks go back on 25 October 2026 in Paris: that day lasts 25 hours.
        val lastDay = midnight(2026, Calendar.OCTOBER, 25)
        val tournament =
            Tournament(name = "t", createdAt = 0, startTime = lastDay, endTime = lastDay)
        val lateThatDay = lastDay + 24 * HOUR + 30 * MINUTE

        assertEquals(lateThatDay, resolve(tournament, lateThatDay))
    }

    private fun resolve(tournament: Tournament, time: Long) =
        ResolveLineupEventTime(fixedClock()) { PARIS }(tournament, time)

    private fun midnight(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance(PARIS).apply {
            clear()
            set(year, month, day)
        }.timeInMillis

    private companion object {
        val PARIS: TimeZone = TimeZone.getTimeZone("Europe/Paris")
        const val MINUTE = 60_000L
        const val HOUR = 60 * MINUTE
        const val DAY = 24 * HOUR
    }

    @Test
    fun shouldReturnNowWhenTournamentHasNoWindow() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 0L, endTime = 0L)

        val result = resolveLineupEventTime(tournament, 150L)

        assertEquals(TEST_NOW, result)
    }
}
