package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.GetRoster
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


@RunWith(MockitoJUnitRunner::class)
internal class GetRosterTests {

    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var playerDao: PlayerRepository
    lateinit var mGetRoster: GetRoster

    private val lineup = Lineup(1L, "A", 1L, 1L,
            1, 3L,1L,1L, null, "hash")

    private val player1 = Player(1L, 1L, "A", 1,
            1, null, 1, 0, 0, "hash")

    private val player2 = Player(2L, 1L, "B", 2,
            2, null, 1, 0, 0, "hash")

    private val player3 = Player(3L, 1L, "C", 3,
            3, null, 1, 0, 0, "hash")

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mGetRoster = GetRoster(playerDao, lineupDao)

        Mockito.`when`(lineupDao.getLineupByIdSingle(1L)).thenReturn(Single.just(lineup))
        Mockito.`when`(playerDao.getPlayers(1L)).thenReturn(Single.just(listOf(player1, player2, player3)))

        val overlays = mutableListOf<PlayerNumberOverlay>().apply {
            add(PlayerNumberOverlay(1L, 1L, player1.id, 42))
            add(PlayerNumberOverlay(3L, 1L, player3.id, 69))
        }

        Mockito.`when`(playerDao.getPlayersNumberOverlay(1L)).thenReturn(Single.just(overlays))
    }

    @Test
    fun shouldReturnAllPlayersIfLineupIdIsNull() {
        val observer = TestObserver<GetRoster.ResponseValue>()
        mGetRoster.executeUseCase(GetRoster.RequestValues(1L, null)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(3, observer.values().first().summary.players.filter { it.status }.size)
        Assert.assertEquals(Constants.STATUS_ALL, observer.values().first().summary.status)
    }

    @Test
    fun shouldReturnAllPlayersIfLineupRosterIsNull() {
        lineup.roster = null
        val observer = TestObserver<GetRoster.ResponseValue>()
        mGetRoster.executeUseCase(GetRoster.RequestValues(1L, 1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(3, observer.values().first().summary.players.filter { it.status }.size)
        Assert.assertEquals(Constants.STATUS_ALL, observer.values().first().summary.status)
    }

    @Test
    fun shouldReturnNoPlayersIfLineupRosterIsEmpty() {
        lineup.roster = ""
        val observer = TestObserver<GetRoster.ResponseValue>()
        mGetRoster.executeUseCase(GetRoster.RequestValues(1L, 1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(0, observer.values().first().summary.players.filter { it.status }.size)
        Assert.assertEquals(Constants.STATUS_PARTIAL, observer.values().first().summary.status)
    }

    @Test
    fun shouldReturnAllPlayersIfLineupRosterIsFull() {
        lineup.roster = "1;2;3"
        val observer = TestObserver<GetRoster.ResponseValue>()
        mGetRoster.executeUseCase(GetRoster.RequestValues(1L, 1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(3, observer.values().first().summary.players.filter { it.status }.size)
        Assert.assertEquals(Constants.STATUS_ALL, observer.values().first().summary.status)
    }

    @Test
    fun shouldReturn2PlayersInRosterSelection() {
        lineup.roster = "1;3"
        val observer = TestObserver<GetRoster.ResponseValue>()
        mGetRoster.executeUseCase(GetRoster.RequestValues(1L, 1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(2, observer.values().first().summary.players.filter { it.status }.size)
        Assert.assertEquals(false, observer.values().first().summary.players.filter { it.player.id == 2L }.first().status)
        Assert.assertEquals(Constants.STATUS_PARTIAL, observer.values().first().summary.status)
    }

    @Test
    fun shouldReturnOverlaysNumber() {
        lineup.roster = "1;2;3"
        val observer = TestObserver<GetRoster.ResponseValue>()
        mGetRoster.executeUseCase(GetRoster.RequestValues(1L, 1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(42, observer.values().first().summary.players[0].playerNumberOverlay?.number)
        Assert.assertEquals(null, observer.values().first().summary.players[1].playerNumberOverlay?.number)
        Assert.assertEquals(69, observer.values().first().summary.players[2].playerNumberOverlay?.number)
    }
}