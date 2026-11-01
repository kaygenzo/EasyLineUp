/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.GetTournamentDisplayStart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

internal class GetTournamentDisplayStartTests {
    private lateinit var getTournamentDisplayStart: GetTournamentDisplayStart

    @Before
    fun init() {
        getTournamentDisplayStart = GetTournamentDisplayStart()
    }

    @Test
    fun shouldUseTheTournamentOwnStartTimeWhenSet() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 100L, endTime = 200L)

        val result = getTournamentDisplayStart(tournament, emptyList())

        assertEquals(100L, result)
    }

    @Test
    fun shouldFallBackToTheEarliestLineupEventTime() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 0L, endTime = 0L)
        val lineups = listOf(
            Lineup(eventTimeInMillis = 300L),
            Lineup(eventTimeInMillis = 150L)
        )

        val result = getTournamentDisplayStart(tournament, lineups)

        assertEquals(150L, result)
    }

    @Test
    fun shouldFallBackToCreationTimeWhenALineupHasNoEventTime() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 0L, endTime = 0L)
        val lineup = Lineup(eventTimeInMillis = 0L, createdTimeInMillis = 500L)

        val result = getTournamentDisplayStart(tournament, listOf(lineup))

        assertEquals(500L, result)
    }

    @Test
    fun shouldReturnNullWhenNoDateInformationIsAvailable() {
        val tournament = Tournament(name = "t", createdAt = 0, startTime = 0L, endTime = 0L)

        val result = getTournamentDisplayStart(tournament, emptyList())

        assertNull(result)
    }
}
