package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.usecases.GetOnlyPlayersInField
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetOnlyPlayersInFieldTests : BaseUseCaseTests() {

    lateinit var useCase: GetOnlyPlayersInField
    lateinit var players: MutableList<PlayerWithPosition>
    private val observer = TestObserver<GetOnlyPlayersInField.ResponseValue>()

    @Before
    fun init() {
        useCase = GetOnlyPlayersInField()

        players = mutableListOf()
        players.add(generate(1L, FieldPosition.PITCHER, 1))
        players.add(generate(2L, FieldPosition.RIGHT_FIELD, 2))
        players.add(generate(3L, FieldPosition.DP_DH, 3))
        players.add(generate(4L, FieldPosition.SHORT_STOP, 4))
        players.add(generate(5L, FieldPosition.SUBSTITUTE, Constants.SUBSTITUTE_ORDER_VALUE))
    }

    private fun generate(playerID: Long, position: FieldPosition, order: Int): PlayerWithPosition {
        val flag = PlayerFieldPosition.FLAG_NONE
        return generate(playerID, position, flag, order)
    }

    private fun startUseCase(players: List<PlayerWithPosition> = this.players) {
        useCase.executeUseCase(GetOnlyPlayersInField.RequestValues(players)).subscribe(observer)
        observer.await()
        observer.assertComplete()
    }

    @Test
    fun shouldReturnEmptyList() {
        startUseCase(mutableListOf())
        Assert.assertTrue(observer.values().first().playersInField.isEmpty())
    }

    @Test
    fun shouldReturnListWithOnlyInfieldersAndOutfielders() {
        startUseCase(players)
        observer.values().first().playersInField.let {
            Assert.assertEquals(3, it.count())
            Assert.assertNotNull(it.firstOrNull { it.playerName == "player1" })
            Assert.assertNotNull(it.firstOrNull { it.playerName == "player2" })
            Assert.assertNotNull(it.firstOrNull { it.playerName == "player4" })
        }
    }
}