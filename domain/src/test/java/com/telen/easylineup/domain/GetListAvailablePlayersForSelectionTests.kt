package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.GetListAvailablePlayersForSelection
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetListAvailablePlayersForSelectionTests {

    lateinit var getListAvailablePlayersForSelection: GetListAvailablePlayersForSelection
    lateinit var players: MutableList<PlayerWithPosition>
    lateinit var roster: MutableList<RosterPlayerStatus>

    @Before
    fun init() {

        val teamID = 1L

        getListAvailablePlayersForSelection = GetListAvailablePlayersForSelection()

        players = mutableListOf()
        players.add(PlayerWithPosition("toto", 1, 1, teamID, null,
                FieldPosition.PITCHER.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,0, 1, 1, 1, 1))
        players.add(PlayerWithPosition("tata", 2, 2, teamID, null,
                FieldPosition.CATCHER.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,2, 0, 2, 1, 2))
        players.add(PlayerWithPosition("titi", 3, 3, teamID, null,
                FieldPosition.CENTER_FIELD.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,9, 3, 3, 1, 4))
        players.add(PlayerWithPosition("tutu", 4, 4, teamID, null,
                FieldPosition.FIRST_BASE.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,10, 0, 4, 1, 8))
        players.add(PlayerWithPosition("tete", 5, 5, teamID, null,
                FieldPosition.SUBSTITUTE.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE, Constants.SUBSTITUTE_ORDER_VALUE, 5, 5, 1, 16))

        roster = mutableListOf()
        roster.add(RosterPlayerStatus(Player(id = 1L, teamId = teamID, name = "toto", shirtNumber =  1, licenseNumber = 1, image = null, positions = 1), true))
        roster.add(RosterPlayerStatus(Player(id = 2L, teamId = teamID, name = "tata", shirtNumber = 2, licenseNumber =  2, image = null, positions =1), true))
        roster.add(RosterPlayerStatus(Player(id = 3L, teamId = teamID, name = "titi", shirtNumber = 3, licenseNumber = 3, image = null, positions =4), true))
        roster.add(RosterPlayerStatus(Player(id = 4L, teamId = teamID, name = "tutu", shirtNumber = 4, licenseNumber = 4, image = null, positions =8), true))
        roster.add(RosterPlayerStatus(Player(id = 5L, teamId = teamID, name = "tete", shirtNumber = 5, licenseNumber = 5, image = null, positions =16), true))
    }

    @Test
    fun shouldTriggerAnErrorIfListEmpty() {
        val observer = TestObserver<GetListAvailablePlayersForSelection.ResponseValue>()
        getListAvailablePlayersForSelection.executeUseCase(GetListAvailablePlayersForSelection.RequestValues(mutableListOf(), FieldPosition.PITCHER, roster))
                .subscribe(observer)
        observer.await()
        observer.assertError(NoSuchElementException::class.java)
    }

    @Test
    fun shouldOnlyReturnPlayersWithoutFieldPosition() {
        val observer = TestObserver<GetListAvailablePlayersForSelection.ResponseValue>()
        getListAvailablePlayersForSelection.executeUseCase(GetListAvailablePlayersForSelection.RequestValues(players, FieldPosition.PITCHER, roster))
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
        getListAvailablePlayersForSelection.executeUseCase(GetListAvailablePlayersForSelection.RequestValues(players, FieldPosition.CATCHER, roster))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(2, observer.values().first().players[0].id)
        Assert.assertEquals(4, observer.values().first().players[1].id)
    }

    @Test
    fun shouldSortPlayersByFieldPositionSecondBase() {
        val observer = TestObserver<GetListAvailablePlayersForSelection.ResponseValue>()
        getListAvailablePlayersForSelection.executeUseCase(GetListAvailablePlayersForSelection.RequestValues(players, FieldPosition.SECOND_BASE, roster))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(4, observer.values().first().players[0].id)
        Assert.assertEquals(2, observer.values().first().players[1].id)
    }

    @Test
    fun shouldReturnAllPlayersWhenRosterIsNull() {
        val observer = TestObserver<GetListAvailablePlayersForSelection.ResponseValue>()
        getListAvailablePlayersForSelection.executeUseCase(GetListAvailablePlayersForSelection.RequestValues(players, FieldPosition.SECOND_BASE, null))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(2, observer.values().first().players.size)
    }

    @Test
    fun shouldRTriggerAnExceptionWhenRosterIsEmpty() {
        val observer = TestObserver<GetListAvailablePlayersForSelection.ResponseValue>()
        getListAvailablePlayersForSelection.executeUseCase(GetListAvailablePlayersForSelection.RequestValues(players, FieldPosition.SECOND_BASE, mutableListOf()))
                .subscribe(observer)
        observer.await()
        observer.assertError(NoSuchElementException::class.java)
    }

    @Test
    fun shouldReturnSomePlayersWhenRosterIsNotNullAndNotEmpty() {
        roster.removeAt(4)
        roster.removeAt(3)
        val observer = TestObserver<GetListAvailablePlayersForSelection.ResponseValue>()
        getListAvailablePlayersForSelection.executeUseCase(GetListAvailablePlayersForSelection.RequestValues(players, FieldPosition.SECOND_BASE, roster))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(1, observer.values().first().players.size)
    }
}