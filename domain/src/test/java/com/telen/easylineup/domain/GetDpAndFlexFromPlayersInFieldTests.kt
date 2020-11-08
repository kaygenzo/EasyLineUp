package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.GetDPAndFlexFromPlayersInField
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetDpAndFlexFromPlayersInFieldTests: BaseUseCaseTests() {

    lateinit var useCase: GetDPAndFlexFromPlayersInField
    lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {

        val teamID = 1L

        useCase = GetDPAndFlexFromPlayersInField()

        players = mutableListOf()
        players.add(generatePlayerWithPosition(1L, teamID, FieldPosition.PITCHER, PlayerFieldPosition.FLAG_NONE, 1, 1))
        players.add(generatePlayerWithPosition(2L, teamID, FieldPosition.RIGHT_FIELD, PlayerFieldPosition.FLAG_NONE, 2, 1))
        players.add(generatePlayerWithPosition(3L, teamID, FieldPosition.DP_DH, PlayerFieldPosition.FLAG_NONE, 3, 1))
        players.add(generatePlayerWithPosition(4L, teamID, FieldPosition.SHORT_STOP, PlayerFieldPosition.FLAG_NONE, 4, 1))
        players.add(generatePlayerWithPosition(5L, teamID, FieldPosition.SUBSTITUTE, PlayerFieldPosition.FLAG_NONE, Constants.SUBSTITUTE_ORDER_VALUE, 1))
    }

    @Test
    fun shouldTriggerNeedAssignPitcherFirstExceptionIfListEmpty() {
        val observer = TestObserver<GetDPAndFlexFromPlayersInField.ResponseValue>()
        useCase.executeUseCase(GetDPAndFlexFromPlayersInField.RequestValues(mutableListOf(), TeamType.BASEBALL.id))
                .subscribe(observer)
        observer.await()
        observer.assertError(NeedAssignPitcherFirstException::class.java)
    }

    @Test
    fun shouldTriggerNeedAssignPitcherFirstExceptionIf_PitcherNotAssigned_Baseball() {
        val observer = TestObserver<GetDPAndFlexFromPlayersInField.ResponseValue>()
        players.removeIf { it.position == FieldPosition.PITCHER.id }
        useCase.executeUseCase(GetDPAndFlexFromPlayersInField.RequestValues(players, TeamType.BASEBALL.id))
                .subscribe(observer)
        observer.await()
        observer.assertError(NeedAssignPitcherFirstException::class.java)
    }

    @Test
    fun shouldReturnDPAndFlexPitcher_baseball() {
        val observer = TestObserver<GetDPAndFlexFromPlayersInField.ResponseValue>()
        players.removeIf { it.position == FieldPosition.DP_DH.id }
        useCase.executeUseCase(GetDPAndFlexFromPlayersInField.RequestValues(players, TeamType.BASEBALL.id))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertNull(observer.values().first().configResult.dp)
        Assert.assertEquals(players[0].toPlayer(), observer.values().first().configResult.flex)
        Assert.assertFalse(observer.values().first().configResult.dpLocked)
        Assert.assertTrue(observer.values().first().configResult.flexLocked)
    }

    @Test
    fun shouldReturnOnlyFlexPitcher_baseball() {
        val observer = TestObserver<GetDPAndFlexFromPlayersInField.ResponseValue>()
        useCase.executeUseCase(GetDPAndFlexFromPlayersInField.RequestValues(players, TeamType.BASEBALL.id))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(players[2].toPlayer(), observer.values().first().configResult.dp)
        Assert.assertEquals(players[0].toPlayer(), observer.values().first().configResult.flex)
        Assert.assertFalse(observer.values().first().configResult.dpLocked)
        Assert.assertTrue(observer.values().first().configResult.flexLocked)
    }

    @Test
    fun shouldReturnDPAndFlexRightField_softball() {
        val observer = TestObserver<GetDPAndFlexFromPlayersInField.ResponseValue>()
        players.first { it.position == FieldPosition.RIGHT_FIELD.id }.flags = PlayerFieldPosition.FLAG_FLEX
        useCase.executeUseCase(GetDPAndFlexFromPlayersInField.RequestValues(players, TeamType.SOFTBALL.id))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(players[2].toPlayer(), observer.values().first().configResult.dp)
        Assert.assertEquals(players[1].toPlayer(), observer.values().first().configResult.flex)
        Assert.assertFalse(observer.values().first().configResult.dpLocked)
        Assert.assertFalse(observer.values().first().configResult.flexLocked)
    }

    @Test
    fun shouldReturnOnlyFlexRightField_softball() {
        val observer = TestObserver<GetDPAndFlexFromPlayersInField.ResponseValue>()
        players.first { it.position == FieldPosition.RIGHT_FIELD.id }.flags = PlayerFieldPosition.FLAG_FLEX
        players.removeIf { it.position == FieldPosition.DP_DH.id }
        useCase.executeUseCase(GetDPAndFlexFromPlayersInField.RequestValues(players, TeamType.SOFTBALL.id))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertNull(observer.values().first().configResult.dp)
        Assert.assertEquals(players[1].toPlayer(), observer.values().first().configResult.flex)
        Assert.assertFalse(observer.values().first().configResult.dpLocked)
        Assert.assertFalse(observer.values().first().configResult.flexLocked)
    }
}