package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.SaveDpAndFlex
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class SaveDpAndFlexTests: BaseUseCaseTests() {

    private lateinit var useCase: SaveDpAndFlex
    lateinit var players: MutableList<PlayerWithPosition>
    @Mock lateinit var playerFieldPositionDao: PlayerFieldPositionRepository
    private val strategy = TeamStrategy.STANDARD
    private val extraHitters = 0

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        val teamID = 1L

        useCase = SaveDpAndFlex(playerFieldPositionDao)

        players = mutableListOf()
        players.add(generatePlayerWithPosition(1L, teamID, FieldPosition.PITCHER, PlayerFieldPosition.FLAG_NONE, 1, 1))
        players.add(generatePlayerWithPosition(2L, teamID, FieldPosition.RIGHT_FIELD, PlayerFieldPosition.FLAG_NONE, 2, 1))
        players.add(generatePlayerWithPosition(3L, teamID, FieldPosition.DP_DH, PlayerFieldPosition.FLAG_NONE, 3, 1))
        players.add(generatePlayerWithPosition(4L, teamID, FieldPosition.SHORT_STOP, PlayerFieldPosition.FLAG_NONE, 4, 1))
        players.add(generatePlayerWithPosition(5L, teamID, FieldPosition.SUBSTITUTE, PlayerFieldPosition.FLAG_NONE, Constants.SUBSTITUTE_ORDER_VALUE, 1))

        Mockito.`when`(playerFieldPositionDao.updatePlayerFieldPositions(any())).thenReturn(Completable.complete())
        Mockito.`when`(playerFieldPositionDao.insertPlayerFieldPositions(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerAnErrorIfLineupIdIsNull() {
        val observer = TestObserver<SaveDpAndFlex.ResponseValue>()
        useCase.executeUseCase(SaveDpAndFlex.RequestValues(lineupID = null, players = players, dp = null, flex = null, strategy = strategy, extraHittersSize = extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfDpNotAssigned() {
        val observer = TestObserver<SaveDpAndFlex.ResponseValue>()
        useCase.executeUseCase(SaveDpAndFlex.RequestValues(lineupID = 1L, players = players, dp = null, flex = players[0].toPlayer(), strategy = strategy, extraHittersSize = extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertError(NeedAssignBothPlayersException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfFlexNotAssigned() {
        val observer = TestObserver<SaveDpAndFlex.ResponseValue>()
        useCase.executeUseCase(SaveDpAndFlex.RequestValues(lineupID = 1L, players = players, dp = players[0].toPlayer(), flex = null, strategy = strategy, extraHittersSize = extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertError(NeedAssignBothPlayersException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfErrorDuringUpdateInDao() {
        Mockito.`when`(playerFieldPositionDao.updatePlayerFieldPositions(any())).thenReturn(Completable.error(Exception()))
        val observer = TestObserver<SaveDpAndFlex.ResponseValue>()
        useCase.executeUseCase(SaveDpAndFlex.RequestValues(lineupID = 1L, players = players, dp = players[0].toPlayer(), flex = players[1].toPlayer(), strategy = strategy, extraHittersSize = extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfErrorDuringInsertInDao() {
        Mockito.`when`(playerFieldPositionDao.insertPlayerFieldPositions(any())).thenReturn(Completable.error(Exception()))
        val observer = TestObserver<SaveDpAndFlex.ResponseValue>()
        useCase.executeUseCase(SaveDpAndFlex.RequestValues(lineupID = 1L, players = players, dp = players[0].toPlayer(), flex = players[1].toPlayer(), strategy = strategy, extraHittersSize = extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldChangeFlagAndOrderOfTheFlex() {
        val observer = TestObserver<SaveDpAndFlex.ResponseValue>()
        useCase.executeUseCase(SaveDpAndFlex.RequestValues(lineupID = 1L, players = players, dp = players[3].toPlayer(), flex = players[1].toPlayer(), strategy = strategy, extraHittersSize = extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(PlayerFieldPosition.FLAG_FLEX, it.firstOrNull { it.playerId == players[1].playerID }?.flags)
            Assert.assertEquals(TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters), it.firstOrNull { it.playerId == players[1].playerID }?.order)
        })
    }

    @Test
    fun shouldChangeFlagAndOrderOfOldFlex() {

        players[0].flags = PlayerFieldPosition.FLAG_FLEX
        players[1].flags = PlayerFieldPosition.FLAG_FLEX
        players[2].flags = PlayerFieldPosition.FLAG_FLEX
        players[3].flags = PlayerFieldPosition.FLAG_FLEX

        val observer = TestObserver<SaveDpAndFlex.ResponseValue>()

        useCase.executeUseCase(SaveDpAndFlex.RequestValues(lineupID = 1L, players = players, dp = players[3].toPlayer(), flex = players[1].toPlayer(), strategy = strategy, extraHittersSize = extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(playerFieldPositionDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            //flex
            Assert.assertEquals(1, it.filter { it.flags and PlayerFieldPosition.FLAG_FLEX > 0 }.count())
            Assert.assertEquals(players[1].playerID, it.firstOrNull { it.flags and PlayerFieldPosition.FLAG_FLEX > 0 }?.playerId)
            Assert.assertEquals(TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters), it.firstOrNull { it.flags and PlayerFieldPosition.FLAG_FLEX > 0 }?.order)

            //others
            Assert.assertEquals(3, it.filter { it.flags and PlayerFieldPosition.FLAG_FLEX == 0 }.count())
            Assert.assertArrayEquals(intArrayOf(1, 2, 3), it.filter { it.flags and PlayerFieldPosition.FLAG_FLEX == 0 }.map { it.order }.sorted().toIntArray())
        })
    }

    @Test
    fun shouldAssignDPToExistingPosition() {
        val observer = TestObserver<SaveDpAndFlex.ResponseValue>()
        useCase.executeUseCase(SaveDpAndFlex.RequestValues(lineupID = 1L, players = players, dp = players[3].toPlayer(), flex = players[1].toPlayer(), strategy = strategy, extraHittersSize = extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(playerFieldPositionDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it.filter { it.position == FieldPosition.DP_DH.id }.count())
            Assert.assertEquals(players[3].playerID, it.firstOrNull { it.position == FieldPosition.DP_DH.id }?.playerId)
            Assert.assertEquals(2, it.firstOrNull { it.position == FieldPosition.DP_DH.id }?.order)
        })
    }

    @Test
    fun shouldAssignDPToNotExistingPosition() {
        players.removeIf { it.position == FieldPosition.DP_DH.id }

        val observer = TestObserver<SaveDpAndFlex.ResponseValue>()
        useCase.executeUseCase(SaveDpAndFlex.RequestValues(lineupID = 1L, players = players, dp = players[3].toPlayer(), flex = players[1].toPlayer(), strategy = strategy, extraHittersSize = extraHitters))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(playerFieldPositionDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it.filter { it.position == FieldPosition.DP_DH.id }.count())
        })

        verify(playerFieldPositionDao).insertPlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it.filter { it.position == FieldPosition.DP_DH.id }.count())
            Assert.assertEquals(players[3].playerID, it.firstOrNull { it.position == FieldPosition.DP_DH.id }?.playerId)
            Assert.assertEquals(2, it.firstOrNull { it.position == FieldPosition.DP_DH.id }?.order)
        })
    }
}