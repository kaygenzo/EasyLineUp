/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.PlayerTournamentStatsProfile
import com.telen.easylineup.domain.usecases.SortPlayersByPlayingTime
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class SortPlayersByPlayingTimeTests {
    private lateinit var sortPlayersByPlayingTime: SortPlayersByPlayingTime

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
        sortPlayersByPlayingTime = SortPlayersByPlayingTime()
    }

    @Test
    fun shouldSortMostPlayedFirst() {
        val result = sortPlayersByPlayingTime(
            listOf(profile(1L, 4), profile(2L, 10), profile(3L, 6))
        )

        assertEquals(listOf(2L, 3L, 1L), result.map { it.playerId })
    }
}
