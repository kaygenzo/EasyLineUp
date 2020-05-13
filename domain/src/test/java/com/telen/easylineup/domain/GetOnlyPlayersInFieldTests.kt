package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.usecases.GetOnlyPlayersInField
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetOnlyPlayersInFieldTests: BaseUseCaseTests() {

    lateinit var useCase: GetOnlyPlayersInField
    lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {

        val teamID = 1L

        useCase = GetOnlyPlayersInField()

        players = mutableListOf()
        players.add(generatePlayerWithPosition(1L, teamID, FieldPosition.PITCHER, PlayerFieldPosition.FLAG_NONE, 1, 1))
        players.add(generatePlayerWithPosition(2L, teamID, FieldPosition.RIGHT_FIELD, PlayerFieldPosition.FLAG_NONE, 2, 1))
        players.add(generatePlayerWithPosition(3L, teamID, FieldPosition.DP_DH, PlayerFieldPosition.FLAG_NONE, 3, 1))
        players.add(generatePlayerWithPosition(4L, teamID, FieldPosition.SHORT_STOP, PlayerFieldPosition.FLAG_NONE, 4, 1))
        players.add(generatePlayerWithPosition(5L, teamID, FieldPosition.SUBSTITUTE, PlayerFieldPosition.FLAG_NONE, Constants.SUBSTITUTE_ORDER_VALUE, 1))
    }

    @Test
    fun shouldReturnEmptyList() {
        val observer = TestObserver<GetOnlyPlayersInField.ResponseValue>()
        useCase.executeUseCase(GetOnlyPlayersInField.RequestValues(mutableListOf()))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertTrue(observer.values().first().playersInField.isEmpty())
    }

    @Test
    fun shouldReturnListWithOnlyInfieldersAndOutfielders() {
        val observer = TestObserver<GetOnlyPlayersInField.ResponseValue>()
        useCase.executeUseCase(GetOnlyPlayersInField.RequestValues(players))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(3, observer.values().first().playersInField.count())
        Assert.assertTrue(observer.values().first().playersInField.firstOrNull() { it.name == "player1" } != null)
        Assert.assertTrue(observer.values().first().playersInField.firstOrNull() { it.name == "player2" } != null)
        Assert.assertTrue(observer.values().first().playersInField.firstOrNull() { it.name == "player4" } != null)
    }
}