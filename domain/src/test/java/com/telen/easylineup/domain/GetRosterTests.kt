package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Lineup
import com.telen.easylineup.repository.model.Player
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.lang.Exception
import java.lang.IllegalArgumentException


@RunWith(MockitoJUnitRunner::class)
class GetRosterTests {

    @Mock lateinit var lineupDao: LineupDao
    @Mock lateinit var playerDao: PlayerDao
    lateinit var mGetRoster: GetRoaster

    private val lineup = Lineup(1L, "A", 1L, 1L,
            1, 1L,1L,null, "hash")

    private val player1 = Player(1L, 1L, "A", 1,
            1, null, 1, "hash")

    private val player2 = Player(2L, 1L, "B", 2,
            2, null, 1, "hash")

    private val player3 = Player(3L, 1L, "C", 3,
            3, null, 1, "hash")

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mGetRoster = GetRoaster(playerDao, lineupDao)

        Mockito.`when`(lineupDao.getLineupByIdSingle(1L)).thenReturn(Single.just(lineup))
        Mockito.`when`(playerDao.getPlayers(1L)).thenReturn(Single.just(listOf(player1, player2, player3)))
    }

    @Test
    fun shouldReturnAllPlayersIfLineupIdIsNull() {
        val observer = TestObserver<GetRoaster.ResponseValue>()
        mGetRoster.executeUseCase(GetRoaster.RequestValues(1L, null)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(3, observer.values().first().players.filter { it.status }.size)
        Assert.assertEquals(Constants.STATUS_ALL, observer.values().first().status)
    }

    @Test
    fun shouldReturnAllPlayersIfLineupRosterIsNull() {
        lineup.roaster = null
        val observer = TestObserver<GetRoaster.ResponseValue>()
        mGetRoster.executeUseCase(GetRoaster.RequestValues(1L, 1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(3, observer.values().first().players.filter { it.status }.size)
        Assert.assertEquals(Constants.STATUS_ALL, observer.values().first().status)
    }

    @Test
    fun shouldReturnNoPlayersIfLineupRosterIsEmpty() {
        lineup.roaster = ""
        val observer = TestObserver<GetRoaster.ResponseValue>()
        mGetRoster.executeUseCase(GetRoaster.RequestValues(1L, 1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(0, observer.values().first().players.filter { it.status }.size)
        Assert.assertEquals(Constants.STATUS_NONE, observer.values().first().status)
    }

    @Test
    fun shouldReturnAllPlayersIfLineupRosterIsFull() {
        lineup.roaster = "1;2;3"
        val observer = TestObserver<GetRoaster.ResponseValue>()
        mGetRoster.executeUseCase(GetRoaster.RequestValues(1L, 1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(3, observer.values().first().players.filter { it.status }.size)
        Assert.assertEquals(Constants.STATUS_ALL, observer.values().first().status)
    }

    @Test
    fun shouldReturn2PlayersInRoasterSelection() {
        lineup.roaster = "1;3"
        val observer = TestObserver<GetRoaster.ResponseValue>()
        mGetRoster.executeUseCase(GetRoaster.RequestValues(1L, 1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(2, observer.values().first().players.filter { it.status }.size)
        Assert.assertEquals(false, observer.values().first().players.filter { it.player.id == 2L }.first().status)
        Assert.assertEquals(Constants.STATUS_NONE, observer.values().first().status)
    }
}