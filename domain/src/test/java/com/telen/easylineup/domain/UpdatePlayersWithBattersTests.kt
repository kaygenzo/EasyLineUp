/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.usecases.UpdatePlayersWithBatters
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class UpdatePlayersWithBattersTests {
    val observer: TestObserver<Void> = TestObserver()
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
        updatePlayersWithBatters = UpdatePlayersWithBatters(testSchedulersProvider())
    }

    @Test
    fun shouldApplyBatterOrderToMatchingPlayers() {
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

        updatePlayersWithBatters(listOf(player1, player2), batters).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(3, player1.order)
        assertEquals(1, player2.order)
    }

    @Test
    fun shouldIgnoreBatterWithNoMatchingPlayer() {
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

        updatePlayersWithBatters(listOf(player1), batters).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(5, player1.order)
    }

    @Test
    fun shouldCompleteWhenBattersListIsEmpty() {
        val player1 = player(playerId = 1L, order = 5)

        updatePlayersWithBatters(listOf(player1), emptyList()).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(5, player1.order)
    }
}
