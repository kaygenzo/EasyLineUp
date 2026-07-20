/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.usecases.GetOnlyPlayersInField
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetOnlyPlayersInFieldTests : BaseUseCaseTests() {
    lateinit var useCase: GetOnlyPlayersInField
    lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {
        useCase = GetOnlyPlayersInField(testDispatcherProvider())

        players = mutableListOf()
        players.add(generate(1L, FieldPosition.PITCHER, 1))
        players.add(generate(2L, FieldPosition.RIGHT_FIELD, 2))
        players.add(generate(3L, FieldPosition.DP_DH, 3))
        players.add(generate(4L, FieldPosition.SHORT_STOP, 4))
        players.add(generate(5L, FieldPosition.SUBSTITUTE, Constants.SUBSTITUTE_ORDER_VALUE))
    }

    private fun generate(playerId: Long, position: FieldPosition, order: Int): PlayerWithPosition {
        val flag = PlayerFieldPosition.FLAG_NONE
        return generate(playerId, position, flag, order)
    }

    private suspend fun startUseCase(
        players: List<PlayerWithPosition> = this.players
    ): List<PlayerWithPosition> {
        val result = useCase(players)
        Assert.assertTrue(result.isSuccess)
        return result.getOrNull()!!
    }

    @Test
    fun shouldReturnEmptyList() = runTest {
        Assert.assertTrue(startUseCase(mutableListOf()).isEmpty())
    }

    @Test
    fun shouldReturnListWithOnlyInfieldersAndOutfielders() = runTest {
        startUseCase(players).let {
            Assert.assertEquals(3, it.count())
            Assert.assertNotNull(it.firstOrNull { it.playerName == "player1" })
            Assert.assertNotNull(it.firstOrNull { it.playerName == "player2" })
            Assert.assertNotNull(it.firstOrNull { it.playerName == "player4" })
        }
    }
}
