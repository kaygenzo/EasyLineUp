/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.ports.StringResourcesProvider
import com.telen.easylineup.domain.usecases.GetBattersState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The cases [GetBattersStateTests]' per-team matrix doesn't reach. */
internal class GetBattersStateEdgeCasesTests {
    private val getBattersState = GetBattersState(
        object : StringResourcesProvider {
            override fun positionShortNames(teamType: Int) = Array(20) { "P$it" }
            override fun unknownPlayerName() = ""
            override fun strategyDisplayNames(teamType: Int): Array<String>? = null
        },
        testDispatcherProvider()
    )

    private fun player(position: Int, order: Int) = PlayerWithPosition(
        playerName = " Player ",
        playerSex = 0,
        shirtNumber = 7,
        licenseNumber = "1",
        teamId = 1L,
        image = null,
        position = position,
        order = order,
        playerId = order.toLong(),
        lineupId = 1L,
        playerPositions = 0
    )

    private suspend fun batters(
        vararg players: PlayerWithPosition,
        teamType: TeamType = TeamType.BASEBALL,
        isDebug: Boolean = false
    ): List<BatterState> = getBattersState(
        players = players.toList(),
        teamType = teamType.id,
        batterSize = 9,
        extraHitterSize = 0,
        isDebug = isDebug,
        isEditable = false
    ).getOrThrow()

    @Test
    fun debugBuildsShowTheOrderOfSubstitutesOutsideTheBattingOrder() = runTest {
        val substitute = player(FieldPosition.SUBSTITUTE.id, order = 10)

        assertFalse(batters(substitute).single().canShowOrder)
        assertTrue(batters(substitute, isDebug = true).single().canShowOrder)
    }

    @Test
    fun baseball5HidesFieldPositions() = runTest {
        val shortStop = player(FieldPosition.SHORT_STOP.id, order = 1)

        assertTrue(batters(shortStop).single().canShowPosition)
        assertFalse(batters(shortStop, teamType = TeamType.BASEBALL_5).single().canShowPosition)
    }

    @Test
    fun aPlayerWithoutAPositionIsTreatedAsASubstituteWithoutDescription() = runTest {
        val batter = batters(player(position = -1, order = 3)).single()

        assertEquals(FieldPosition.SUBSTITUTE, batter.playerPosition)
        assertEquals("", batter.playerPositionDesc)
    }

    @Test
    fun namesAreTrimmed() = runTest {
        val shortStop = player(FieldPosition.SHORT_STOP.id, order = 1)

        assertEquals("Player", batters(shortStop).single().playerName)
    }
}
