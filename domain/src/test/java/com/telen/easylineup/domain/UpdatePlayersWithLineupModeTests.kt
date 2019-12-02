package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.*
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.PlayerWithPosition
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UpdatePlayersWithLineupModeTests {

    lateinit var updatePlayersWithLineupMode: UpdatePlayersWithLineupMode
    lateinit var players: MutableList<PlayerWithPosition>
    var observer = TestObserver<UpdatePlayersWithLineupMode.ResponseValue>()

    @Mock lateinit var lineupDao: PlayerFieldPositionsDao

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        updatePlayersWithLineupMode = UpdatePlayersWithLineupMode(lineupDao)
        players = mutableListOf()
        players.add(PlayerWithPosition("toto", 1, 1, 1, null,
                FieldPosition.SECOND_BASE.position, 0f, 0f, 0, 1, 1, 1, 1))
        players.add(PlayerWithPosition("tata", 2, 2, 1, null,
                FieldPosition.CATCHER.position, 0f, 0f, 2, 2, 2, 1, 2))
        players.add(PlayerWithPosition("titi", 3, 3, 1, null,
                FieldPosition.CENTER_FIELD.position, 0f, 0f, 4, 3, 3, 1, 4))
        players.add(PlayerWithPosition("tutu", 4, 4, 1, null,
                FieldPosition.FIRST_BASE.position, 0f, 0f, 6, 4, 4, 1, 8))
        players.add(PlayerWithPosition("tete", 5, 5, 1, null,
                FieldPosition.SUBSTITUTE.position, 0f, 0f, Constants.SUBSTITUTE_ORDER_VALUE, 5, 5, 1, 16))

        Mockito.`when`(lineupDao.updatePlayerFieldPosition(any())).thenReturn(Completable.complete())
        Mockito.`when`(lineupDao.deletePosition(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldDoNothingIfListEmptyAndDHEnabled() {
        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(mutableListOf(), true))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verifyZeroInteractions(lineupDao)
    }

    @Test
    fun shouldDoNothingIfListEmptyAndDHDisabled() {
        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(mutableListOf(), false))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verifyZeroInteractions(lineupDao)
    }

    @Test
    fun shouldDoNothingIfDHEnabledAndNoPitcherAssigned() {
        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(players, true))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verifyZeroInteractions(lineupDao)
    }

    @Test
    fun shouldDoNothingIfDHDisabledAndNoPitcherOrDHAssigned() {
        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(players, false))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verifyZeroInteractions(lineupDao)
    }

    @Test
    fun shouldUpdatePitcherOrderTo_10_ifAssignedAndDHEnabled() {
        players[0].position = FieldPosition.PITCHER.position

        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(players, true))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updatePlayerFieldPosition(check {
            Assert.assertEquals(Constants.ORDER_PITCHER_WHEN_DH, it.order)
            Assert.assertEquals(FieldPosition.PITCHER.position, it.position)
        })
        verify(lineupDao, never()).deletePosition(any())
    }

    @Test
    fun shouldDeletePitcherAndDHifAssignedAndDHDisabled() {
        players[0].position = FieldPosition.PITCHER.position
        players[1].position = FieldPosition.DH.position

        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(players, false))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao, times(2)).deletePosition(check {
            Assert.assertEquals(true, it.position == FieldPosition.PITCHER.position || it.position == FieldPosition.DH.position)
        })
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }
}