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

    /////// BASEBALL TESTS /////////

    @Test
    fun shouldSwitch_baseball_Another_Player_With_Another_Player() {
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
            assertVerification(it[0],4, 4, FieldPosition.CENTER_FIELD.position, PlayerFieldPosition.FLAG_NONE, 6)
            assertVerification(it[1],3, 3, FieldPosition.FIRST_BASE.position, PlayerFieldPosition.FLAG_NONE, 4)
        })
    }

    @Test
    fun shouldSwitch_baseball_DH_and_Pitcher() {
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
            assertVerification(it[0],6, 6, FieldPosition.PITCHER.position, PlayerFieldPosition.FLAG_FLEX, Constants.ORDER_PITCHER_WHEN_DH)
            assertVerification(it[1],1, 1, FieldPosition.DP_DH.position, PlayerFieldPosition.FLAG_NONE, 8)
        })
    }

    @Test
    fun shouldSwitch_baseball_DH_and_Another_Player() {
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
            assertVerification(it[0],6, 6, FieldPosition.FIRST_BASE.position, PlayerFieldPosition.FLAG_NONE, 8)
            assertVerification(it[1],4, 4, FieldPosition.DP_DH.position, PlayerFieldPosition.FLAG_NONE, 6)
        })
    }

    @Test
    fun shouldSwitch_baseball_Pitcher_and_Another_Player() {
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
            assertVerification(it[0],1, 1, FieldPosition.FIRST_BASE.position, PlayerFieldPosition.FLAG_NONE, 6)
            assertVerification(it[1],4, 4, FieldPosition.PITCHER.position, PlayerFieldPosition.FLAG_FLEX, Constants.ORDER_PITCHER_WHEN_DH)
        })
    }

    @Test
    fun shouldSwitch_baseball_Pitcher_and_Empty() {
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
            assertVerification(it[0],1, 1, FieldPosition.SECOND_BASE.position, PlayerFieldPosition.FLAG_NONE, 1)
        })
    }

    @Test
    fun shouldSwitch_baseball_DH_and_Empty() {
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
            assertVerification(it[0],6, 6, FieldPosition.SECOND_BASE.position, PlayerFieldPosition.FLAG_NONE, 8)
        })
    }

    @Test
    fun shouldSwitch_baseball_Another_and_Empty() {
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
            assertVerification(it[0],4, 4, FieldPosition.SECOND_BASE.position, PlayerFieldPosition.FLAG_NONE, 6)
        })
    }

    //////// SOFTBALL TESTS /////////

    /*
    * Rules:
    * - Flex can be anyone except DP.
    * - If FLEX exists, its order is 10
    * - If 2 players switch their positions:
    *   if it is 2 normal players, only positions change
    *   if it is a FLEX and a normal player, only positions change
    *   if it is the DP and a normal player, only positions change
    *   if is is the DP and the FLEX, switch position, flags and order
    */

    @Test
    fun shouldSwitch_softball_normal_with_normal() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.SOFTBALL.id
        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.FIRST_BASE, position2 = FieldPosition.CENTER_FIELD,
                lineupMode = lineupMode, teamType = teamType
        )).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            assertVerification(it[0],4, 4, FieldPosition.CENTER_FIELD.position, PlayerFieldPosition.FLAG_NONE, 6)
            assertVerification(it[1],3, 3, FieldPosition.FIRST_BASE.position, PlayerFieldPosition.FLAG_NONE, 4)
        })
    }

    @Test
    fun shouldSwitch_softball_flex_with_normal() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.SOFTBALL.id

        players.first { it.position == FieldPosition.PITCHER.position }.apply {
            flags = PlayerFieldPosition.FLAG_NONE
            order = 1
        }

        players.first { it.position == FieldPosition.CATCHER.position }.apply {
            flags = PlayerFieldPosition.FLAG_FLEX
            order = Constants.ORDER_PITCHER_WHEN_DH
        }

        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.CATCHER, position2 = FieldPosition.CENTER_FIELD,
                lineupMode = lineupMode, teamType = teamType
        )).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            assertVerification(it[0],2, 2, FieldPosition.CENTER_FIELD.position, PlayerFieldPosition.FLAG_FLEX, Constants.ORDER_PITCHER_WHEN_DH)
            assertVerification(it[1],3, 3, FieldPosition.CATCHER.position, PlayerFieldPosition.FLAG_NONE, 4)
        })
    }

    @Test
    fun shouldSwitch_softball_dp_with_normal() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.SOFTBALL.id

        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.DP_DH, position2 = FieldPosition.CENTER_FIELD,
                lineupMode = lineupMode, teamType = teamType
        )).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            assertVerification(it[0],6, 6, FieldPosition.CENTER_FIELD.position, PlayerFieldPosition.FLAG_NONE, 8)
            assertVerification(it[1],3, 3, FieldPosition.DP_DH.position, PlayerFieldPosition.FLAG_NONE, 4)
        })
    }

    @Test
    fun shouldSwitch_softball_flex_with_dp() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.SOFTBALL.id

        players.first { it.position == FieldPosition.PITCHER.position }.apply {
            flags = PlayerFieldPosition.FLAG_NONE
            order = 1
        }

        players.first { it.position == FieldPosition.CATCHER.position }.apply {
            flags = PlayerFieldPosition.FLAG_FLEX
            order = Constants.ORDER_PITCHER_WHEN_DH
        }

        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.CATCHER, position2 = FieldPosition.DP_DH,
                lineupMode = lineupMode, teamType = teamType
        )).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            assertVerification(it[0],2, 2, FieldPosition.DP_DH.position, PlayerFieldPosition.FLAG_NONE, 8)
            assertVerification(it[1],6, 6, FieldPosition.CATCHER.position, PlayerFieldPosition.FLAG_FLEX, Constants.ORDER_PITCHER_WHEN_DH)
        })
    }

    @Test
    fun shouldSwitch_softball_dp_with_flex() {
        val lineupMode = MODE_ENABLED
        val teamType = TeamType.SOFTBALL.id

        players.first { it.position == FieldPosition.PITCHER.position }.apply {
            flags = PlayerFieldPosition.FLAG_NONE
            order = 1
        }

        players.first { it.position == FieldPosition.CATCHER.position }.apply {
            flags = PlayerFieldPosition.FLAG_FLEX
            order = Constants.ORDER_PITCHER_WHEN_DH
        }

        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(players = players,
                position1 = FieldPosition.DP_DH, position2 = FieldPosition.CATCHER,
                lineupMode = lineupMode, teamType = teamType
        )).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            assertVerification(it[0],6, 6, FieldPosition.CATCHER.position, PlayerFieldPosition.FLAG_FLEX, Constants.ORDER_PITCHER_WHEN_DH)
            assertVerification(it[1],2, 2, FieldPosition.DP_DH.position, PlayerFieldPosition.FLAG_NONE, 8)
        })
    }

    private fun assertVerification(pfp: PlayerFieldPosition, id: Long, playerID: Long, position: Int, flags: Int, order: Int) {
        Assert.assertEquals(id, pfp.id)
        Assert.assertEquals(playerID, pfp.playerId)
        Assert.assertEquals(position, pfp.position)
        Assert.assertEquals(flags, pfp.flags)
        Assert.assertEquals(order, pfp.order)
    }
}