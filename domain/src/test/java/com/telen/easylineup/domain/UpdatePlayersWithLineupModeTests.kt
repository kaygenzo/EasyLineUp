package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.*
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.UpdatePlayersWithLineupMode
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
internal class UpdatePlayersWithLineupModeTests {

    lateinit var updatePlayersWithLineupMode: UpdatePlayersWithLineupMode
    lateinit var players: MutableList<PlayerWithPosition>
    var observer = TestObserver<UpdatePlayersWithLineupMode.ResponseValue>()
    private val extraHitters = 0
    private val strategy = TeamStrategy.STANDARD

    @Mock lateinit var lineupDao: PlayerFieldPositionRepository

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        updatePlayersWithLineupMode = UpdatePlayersWithLineupMode(lineupDao)
        players = mutableListOf()
        players.add(PlayerWithPosition("toto", 1, 1, 1, null,
                FieldPosition.SECOND_BASE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE,0, 1, 1, 1, 1))
        players.add(PlayerWithPosition("tata", 2, 2, 1, null,
                FieldPosition.CATCHER.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE,2, 2, 2, 1, 2))
        players.add(PlayerWithPosition("titi", 3, 3, 1, null,
                FieldPosition.CENTER_FIELD.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE,4, 3, 3, 1, 4))
        players.add(PlayerWithPosition("tutu", 4, 4, 1, null,
                FieldPosition.FIRST_BASE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE,6, 4, 4, 1, 8))
        players.add(PlayerWithPosition("tete", 5, 5, 1, null,
                FieldPosition.SUBSTITUTE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE, Constants.SUBSTITUTE_ORDER_VALUE, 5, 5, 1, 16))

        Mockito.`when`(lineupDao.updatePlayerFieldPosition(any())).thenReturn(Completable.complete())
        Mockito.`when`(lineupDao.deletePosition(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerAnExceptionIfTeamTypeIsUnknown() {
        val teamType = 10
        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(mutableListOf(), true, teamType, strategy, extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertError(IllegalArgumentException::class.java)
    }

    @Test
    fun shouldDoNothingIfListEmptyAndDPEnabled() {
        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(mutableListOf(), true, TeamType.BASEBALL.id, strategy, extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verifyZeroInteractions(lineupDao)
    }

    @Test
    fun shouldDoNothingIfListEmptyAndDPDisabled() {
        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(mutableListOf(), false, TeamType.BASEBALL.id, strategy, extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verifyZeroInteractions(lineupDao)
    }

    @Test
    fun shouldDoNothingIfDesignatedPlayerEnabledAndSoftballTeam() {
        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(mutableListOf(), true, TeamType.SOFTBALL.id, strategy, extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verifyZeroInteractions(lineupDao)
    }

    @Test
    fun shouldDoNothingIfDPEnabledAndNoPitcherAssigned() {
        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(players, true, TeamType.BASEBALL.id, strategy, extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verifyZeroInteractions(lineupDao)
    }

    @Test
    fun shouldDoNothingIfDPDisabledAndNoPitcherOrDPAssigned() {
        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(players, false, TeamType.BASEBALL.id, strategy, extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verifyZeroInteractions(lineupDao)
    }

    @Test
    fun shouldUpdatePitcherOrderTo_10_ifAssignedAndDPEnabled() {
        players[0].position = FieldPosition.PITCHER.id

        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(players, true, TeamType.BASEBALL.id, strategy, extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updatePlayerFieldPosition(check {
            Assert.assertEquals(TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters), it.order)
            Assert.assertEquals(PlayerFieldPosition.FLAG_FLEX, it.flags)
            Assert.assertEquals(FieldPosition.PITCHER.id, it.position)
        })
        verify(lineupDao, never()).deletePosition(any())
    }

    @Test
    fun shouldDeletePitcherAndDPifAssignedAndDPDisabled() {
        players[0].apply {
            position = FieldPosition.PITCHER.id
            flags = PlayerFieldPosition.FLAG_FLEX
        }
        players[1].position = FieldPosition.DP_DH.id

        updatePlayersWithLineupMode.executeUseCase(UpdatePlayersWithLineupMode.RequestValues(players, false, TeamType.BASEBALL.id, strategy, extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao, times(2)).deletePosition(check {
            Assert.assertEquals(true, it.flags and PlayerFieldPosition.FLAG_FLEX > 0 || it.position == FieldPosition.DP_DH.id)
        })
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }
}