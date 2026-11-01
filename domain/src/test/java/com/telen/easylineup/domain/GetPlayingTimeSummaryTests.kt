/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.PlayerTournamentStatsProfile
import com.telen.easylineup.domain.usecases.GetPlayingTimeSummary
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class GetPlayingTimeSummaryTests {
    private lateinit var getPlayingTimeSummary: GetPlayingTimeSummary

    private fun profile(id: Long, totalGames: Int) = PlayerTournamentStatsProfile(
        playerId = id,
        playerName = "p$id",
        shirtNumber = id.toInt(),
        image = null,
        totalGames = totalGames,
        positionCounts = emptyMap()
    )

    @Before
    fun init() {
        getPlayingTimeSummary = GetPlayingTimeSummary()
    }

    @Test
    fun shouldComputeAverageLowestAndHighest() {
        val result = getPlayingTimeSummary(
            listOf(profile(1L, 10), profile(2L, 4), profile(3L, 6))
        )

        assertEquals(20f / 3f, result.average, 0.001f)
        assertEquals(4, result.lowest)
        assertEquals(10, result.highest)
    }

    @Test
    fun shouldReturnZeroedSummaryWhenThereAreNoPlayers() {
        val result = getPlayingTimeSummary(emptyList())

        assertEquals(0f, result.average, 0.001f)
        assertEquals(0, result.lowest)
        assertEquals(0, result.highest)
    }
}
