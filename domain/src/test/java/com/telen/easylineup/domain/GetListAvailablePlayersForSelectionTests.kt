package com.telen.easylineup.domain

import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.model.PlayerWithPosition
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GetListAvailablePlayersForSelectionTests {

    lateinit var getListAvailablePlayersForSelection: GetListAvailablePlayersForSelection
    lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {
        getListAvailablePlayersForSelection = GetListAvailablePlayersForSelection()
        players = mutableListOf()
        players.add(PlayerWithPosition("toto", 1, 1, 1, null,
                FieldPosition.PITCHER.position, 0f, 0f, 0, 1, 1, 1, 1))
        players.add(PlayerWithPosition("tata", 2, 2, 1, null,
                FieldPosition.CATCHER.position, 0f, 0f, 2, 0, 2, 1, 2))
        players.add(PlayerWithPosition("titi", 3, 3, 1, null,
                FieldPosition.CENTER_FIELD.position, 0f, 0f, 9, 3, 3, 1, 4))
        players.add(PlayerWithPosition("tutu", 4, 4, 1, null,
                FieldPosition.FIRST_BASE.position, 0f, 0f, 10, 0, 4, 1, 8))
        players.add(PlayerWithPosition("tete", 5, 5, 1, null,
                FieldPosition.SUBSTITUTE.position, 0f, 0f, Constants.SUBSTITUTE_ORDER_VALUE, 5, 5, 1, 16))
    }

    @Test
    fun shouldTriggerAnErrorIfListEmpty() {
        val observer = TestObserver<GetListAvailablePlayersForSelection.ResponseValue>()
        getListAvailablePlayersForSelection.executeUseCase(GetListAvailablePlayersForSelection.RequestValues(mutableListOf(), FieldPosition.PITCHER))
                .subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldOnlyReturnPlayersWithoutFieldPosition() {
        val observer = TestObserver<GetListAvailablePlayersForSelection.ResponseValue>()
        getListAvailablePlayersForSelection.executeUseCase(GetListAvailablePlayersForSelection.RequestValues(players, FieldPosition.PITCHER))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(2, observer.values().first().players.size)
        Assert.assertEquals(1, observer.values().first().players.filter { it.id == 2L }.size)
        Assert.assertEquals(1, observer.values().first().players.filter { it.id == 4L }.size)
    }

    @Test
    fun shouldSortPlayersByFieldPositionCatcher() {
        val observer = TestObserver<GetListAvailablePlayersForSelection.ResponseValue>()
        getListAvailablePlayersForSelection.executeUseCase(GetListAvailablePlayersForSelection.RequestValues(players, FieldPosition.CATCHER))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(2, observer.values().first().players[0].id)
        Assert.assertEquals(4, observer.values().first().players[1].id)
    }

    @Test
    fun shouldSortPlayersByFieldPositionSecondBase() {
        val observer = TestObserver<GetListAvailablePlayersForSelection.ResponseValue>()
        getListAvailablePlayersForSelection.executeUseCase(GetListAvailablePlayersForSelection.RequestValues(players, FieldPosition.SECOND_BASE))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(4, observer.values().first().players[0].id)
        Assert.assertEquals(2, observer.values().first().players[1].id)
    }
}