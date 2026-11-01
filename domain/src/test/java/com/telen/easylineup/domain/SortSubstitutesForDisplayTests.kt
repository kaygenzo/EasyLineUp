/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.usecases.SortSubstitutesForDisplay
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class SortSubstitutesForDisplayTests {
    private lateinit var sortSubstitutesForDisplay: SortSubstitutesForDisplay

    private fun player(
        id: Long,
        name: String,
        order: Int,
        position: FieldPosition = FieldPosition.SUBSTITUTE
    ) = PlayerWithPosition(
        playerName = name,
        playerSex = 0,
        shirtNumber = id.toInt(),
        licenseNumber = id,
        teamId = 1L,
        image = null,
        position = position.id,
        order = order,
        playerId = id,
        lineupId = 1L,
        playerPositions = 0
    )

    @Before
    fun init() {
        sortSubstitutesForDisplay = SortSubstitutesForDisplay()
    }

    @Test
    fun shouldExcludeNonSubstitutePlayers() {
        val players = listOf(
            player(1L, "Alice", order = 1, position = FieldPosition.PITCHER),
            player(2L, "Bob", order = 1)
        )

        val result = sortSubstitutesForDisplay(players)

        assertEquals(listOf(2L), result.map { it.playerId })
    }

    @Test
    fun shouldSortByBattingOrderFirst() {
        val players = listOf(
            player(1L, "Zoe", order = 2),
            player(2L, "Amy", order = 1)
        )

        val result = sortSubstitutesForDisplay(players)

        assertEquals(listOf("Amy", "Zoe"), result.map { it.playerName })
    }

    @Test
    fun shouldFallBackToAlphabeticalOrderWhenOrdersMatch() {
        val players = listOf(
            player(1L, "Zoe", order = 0),
            player(2L, "Amy", order = 0)
        )

        val result = sortSubstitutesForDisplay(players)

        assertEquals(listOf("Amy", "Zoe"), result.map { it.playerName })
    }
}
