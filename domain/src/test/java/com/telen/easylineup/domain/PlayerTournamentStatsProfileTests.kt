/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerTournamentStatsProfile
import com.telen.easylineup.domain.model.playedPositions
import com.telen.easylineup.domain.model.sortedByName
import org.junit.Assert.assertEquals
import org.junit.Test

internal class PlayerTournamentStatsProfileTests {
    private fun profile(name: String, vararg positions: FieldPosition) =
        PlayerTournamentStatsProfile(
            playerId = name.hashCode().toLong(),
            playerName = name,
            shirtNumber = 1,
            image = null,
            totalGames = positions.size,
            positionCounts = positions.associateWith { 1 }
        )

    @Test
    fun playedPositionsAreDistinctAndInFieldOrder() {
        val profiles = listOf(
            profile("a", FieldPosition.CENTER_FIELD, FieldPosition.PITCHER),
            profile("b", FieldPosition.PITCHER, FieldPosition.CATCHER),
            profile("c")
        )

        assertEquals(
            listOf(FieldPosition.PITCHER, FieldPosition.CATCHER, FieldPosition.CENTER_FIELD),
            profiles.playedPositions()
        )
    }

    @Test
    fun playedPositionsKeepAPositionOnlyOneStrategyHas() {
        val slowpitch = profile("a", FieldPosition.SLOWPITCH_RF)

        assertEquals(listOf(FieldPosition.SLOWPITCH_RF), listOf(slowpitch).playedPositions())
    }

    @Test
    fun noProfileMeansNoPosition() {
        val none = emptyList<PlayerTournamentStatsProfile>()

        assertEquals(emptyList<FieldPosition>(), none.playedPositions())
    }

    @Test
    fun sortedByNameIgnoresCase() {
        val profiles = listOf(profile("zoe"), profile("Adam"), profile("bob"))

        assertEquals(listOf("Adam", "bob", "zoe"), profiles.sortedByName().map { it.playerName })
    }
}
