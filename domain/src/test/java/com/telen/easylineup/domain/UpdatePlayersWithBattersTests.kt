/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.usecases.UpdatePlayersWithBatters
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class UpdatePlayersWithBattersTests {
    lateinit var updatePlayersWithBatters: UpdatePlayersWithBatters

    private fun player(playerId: Long, order: Int = 0) = PlayerWithPosition(
        playerName = "player$playerId",
        playerSex = 0,
        shirtNumber = playerId.toInt(),
        licenseNumber = playerId,
        teamId = 1L,
        image = null,
        position = FieldPosition.PITCHER.id,
        order = order,
        playerId = playerId,
        lineupId = 1L,
        playerPositions = 0
    )

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        updatePlayersWithBatters = UpdatePlayersWithBatters(testDispatcherProvider())
    }

    @Test
    fun shouldApplyBatterOrderToMatchingPlayers() = runTest {
        val player1 = player(playerId = 1L)
        val player2 = player(playerId = 2L)
        val batters = listOf(
            BatterState(
                playerId = 1L, playerFlag = 0, playerOrder = 3, playerName = "player1",
                playerNumber = "1", playerPosition = FieldPosition.PITCHER,
                playerPositionDesc = "", canShowPosition = true, canMove = true,
                canShowDescription = true, canShowOrder = true, applyBackground = false,
                isEditable = true
            ),
            BatterState(
                playerId = 2L, playerFlag = 0, playerOrder = 1, playerName = "player2",
                playerNumber = "2", playerPosition = FieldPosition.PITCHER,
                playerPositionDesc = "", canShowPosition = true, canMove = true,
                canShowDescription = true, canShowOrder = true, applyBackground = false,
                isEditable = true
            )
        )

        val result = updatePlayersWithBatters(listOf(player1, player2), batters)

        assertTrue(result.isSuccess)
        assertEquals(3, player1.order)
        assertEquals(1, player2.order)
    }

    @Test
    fun shouldIgnoreBatterWithNoMatchingPlayer() = runTest {
        val player1 = player(playerId = 1L, order = 5)
        val batters = listOf(
            BatterState(
                playerId = 42L, playerFlag = 0, playerOrder = 9, playerName = "ghost",
                playerNumber = "9", playerPosition = FieldPosition.PITCHER,
                playerPositionDesc = "", canShowPosition = true, canMove = true,
                canShowDescription = true, canShowOrder = true, applyBackground = false,
                isEditable = true
            )
        )

        val result = updatePlayersWithBatters(listOf(player1), batters)

        assertTrue(result.isSuccess)
        assertEquals(5, player1.order)
    }

    @Test
    fun shouldCompleteWhenBattersListIsEmpty() = runTest {
        val player1 = player(playerId = 1L, order = 5)

        val result = updatePlayersWithBatters(listOf(player1), emptyList())

        assertTrue(result.isSuccess)
        assertEquals(5, player1.order)
    }
}
