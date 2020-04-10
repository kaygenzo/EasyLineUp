package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.*
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
class SwitchPlayersPositionTests {

    @Mock lateinit var playerFieldPositionsDao: PlayerFieldPositionsDao
    lateinit var mSwitchPlayersPosition: SwitchPlayersPosition

    private lateinit var player2bis: PlayerWithPosition
    private val players = mutableListOf<PlayerWithPosition>()

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mSwitchPlayersPosition = SwitchPlayersPosition(playerFieldPositionsDao)

        players.add(PlayerWithPosition("toto", 1, 1, 1, null,
                FieldPosition.PITCHER.position, 0f, 0f, PlayerFieldPosition.FLAG_FLEX,10, 1, 1, 1, 1))
        players.add(PlayerWithPosition("tata", 2, 2, 1, null,
                FieldPosition.CATCHER.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,2, 2, 2, 1, 2))
        players.add(PlayerWithPosition("titi", 3, 3, 1, null,
                FieldPosition.CENTER_FIELD.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,4, 3, 3, 1, 4))
        players.add(PlayerWithPosition("tutu", 4, 4, 1, null,
                FieldPosition.FIRST_BASE.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,6, 4, 4, 1, 8))
        players.add(PlayerWithPosition("tete", 5, 5, 1, null,
                FieldPosition.SUBSTITUTE.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE, Constants.SUBSTITUTE_ORDER_VALUE, 5, 5, 1, 16))
        players.add(PlayerWithPosition("toutou", 6, 6, 1, null,
                FieldPosition.DP_DH.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE, 8, 6, 6, 1, 16))

        player2bis = PlayerWithPosition(
                "tata", 2, 2, 1,
                null, 2, 0f,0f, PlayerFieldPosition.FLAG_NONE,2, 2,
                1, 1, 1)

        Mockito.`when`(playerFieldPositionsDao.updatePlayerFieldPositions(any())).thenReturn(Completable.complete())
    }

    /////// BASEBALL TESTS /////////

    @Test
    fun shouldTriggerSamePlayersException() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.BASEBALL.id
        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.PITCHER, position2 = FieldPosition.PITCHER,
                lineupMode = lineupMode, teamType = teamType)).subscribe(observer)
        observer.await()
        observer.assertError(SamePlayerException::class.java)
    }

    @Test
    fun shouldSwitch_Another_Player_With_Another_Player() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.BASEBALL.id
        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.FIRST_BASE, position2 = FieldPosition.CENTER_FIELD,
                lineupMode = lineupMode, teamType = teamType
        )).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(4, it[0].id)
            Assert.assertEquals(4, it[0].playerId)
            Assert.assertEquals(FieldPosition.CENTER_FIELD.position, it[0].position)

            Assert.assertEquals(3, it[1].id)
            Assert.assertEquals(3, it[1].playerId)
            Assert.assertEquals(FieldPosition.FIRST_BASE.position, it[1].position)
        })
    }

    @Test
    fun shouldSwitch_DH_and_Pitcher() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.BASEBALL.id
        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.DP_DH, position2 = FieldPosition.PITCHER,
                lineupMode = lineupMode, teamType = teamType
        )).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(6, it[0].id)
            Assert.assertEquals(6, it[0].playerId)
            Assert.assertEquals(FieldPosition.PITCHER.position, it[0].position)
            Assert.assertEquals(PlayerFieldPosition.FLAG_FLEX, it[0].flags)
            Assert.assertEquals(Constants.ORDER_PITCHER_WHEN_DH, it[0].order)

            Assert.assertEquals(1, it[1].id)
            Assert.assertEquals(1, it[1].playerId)
            Assert.assertEquals(FieldPosition.DP_DH.position, it[1].position)
            Assert.assertEquals(PlayerFieldPosition.FLAG_NONE, it[1].flags)
            Assert.assertEquals(1, it[1].order)
        })
    }

    @Test
    fun shouldSwitch_DH_and_Another_Player() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.BASEBALL.id
        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.DP_DH, position2 = FieldPosition.FIRST_BASE,
                lineupMode = lineupMode, teamType = teamType
        )).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(6, it[0].id)
            Assert.assertEquals(6, it[0].playerId)
            Assert.assertEquals(FieldPosition.FIRST_BASE.position, it[0].position)
            Assert.assertEquals(PlayerFieldPosition.FLAG_NONE, it[0].flags)
            Assert.assertEquals(8, it[0].order)

            Assert.assertEquals(4, it[1].id)
            Assert.assertEquals(4, it[1].playerId)
            Assert.assertEquals(FieldPosition.DP_DH.position, it[1].position)
            Assert.assertEquals(PlayerFieldPosition.FLAG_NONE, it[1].flags)
            Assert.assertEquals(6, it[1].order)
        })
    }

    @Test
    fun shouldSwitch_Pitcher_and_Another_Player() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.BASEBALL.id
        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.PITCHER, position2 = FieldPosition.FIRST_BASE,
                lineupMode = lineupMode, teamType = teamType
        )).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it[0].id)
            Assert.assertEquals(1, it[0].playerId)
            Assert.assertEquals(FieldPosition.FIRST_BASE.position, it[0].position)
            Assert.assertEquals(PlayerFieldPosition.FLAG_NONE, it[0].flags)
            Assert.assertEquals(6, it[0].order)

            Assert.assertEquals(4, it[1].id)
            Assert.assertEquals(4, it[1].playerId)
            Assert.assertEquals(FieldPosition.PITCHER.position, it[1].position)
            Assert.assertEquals(PlayerFieldPosition.FLAG_FLEX, it[1].flags)
            Assert.assertEquals(Constants.ORDER_PITCHER_WHEN_DH, it[1].order)
        })
    }

    @Test
    fun shouldSwitch_Pitcher_and_Empty() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.BASEBALL.id
        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.PITCHER, position2 = FieldPosition.SECOND_BASE,
                lineupMode = lineupMode, teamType = teamType
        )).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it.size)

            Assert.assertEquals(1, it[0].id)
            Assert.assertEquals(1, it[0].playerId)
            Assert.assertEquals(FieldPosition.SECOND_BASE.position, it[0].position)
            Assert.assertEquals(PlayerFieldPosition.FLAG_NONE, it[0].flags)
            Assert.assertEquals(1, it[0].order)
        })
    }

    @Test
    fun shouldSwitch_DH_and_Empty() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.BASEBALL.id
        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.DP_DH, position2 = FieldPosition.SECOND_BASE,
                lineupMode = lineupMode, teamType = teamType
        )).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it.size)

            Assert.assertEquals(6, it[0].id)
            Assert.assertEquals(6, it[0].playerId)
            Assert.assertEquals(FieldPosition.SECOND_BASE.position, it[0].position)
            Assert.assertEquals(PlayerFieldPosition.FLAG_NONE, it[0].flags)
            Assert.assertEquals(8, it[0].order)
        })
    }

    @Test
    fun shouldSwitch_Another_and_Empty() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.BASEBALL.id
        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.FIRST_BASE, position2 = FieldPosition.SECOND_BASE,
                lineupMode = lineupMode, teamType = teamType
        )).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it.size)

            Assert.assertEquals(4, it[0].id)
            Assert.assertEquals(4, it[0].playerId)
            Assert.assertEquals(FieldPosition.SECOND_BASE.position, it[0].position)
            Assert.assertEquals(PlayerFieldPosition.FLAG_NONE, it[0].flags)
            Assert.assertEquals(6, it[0].order)
        })
    }
}