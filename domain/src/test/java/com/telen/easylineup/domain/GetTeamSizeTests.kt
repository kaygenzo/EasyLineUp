package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.tiles.KEY_DATA_SIZE
import com.telen.easylineup.domain.model.tiles.KEY_DATA_TEAM_IMAGE
import com.telen.easylineup.domain.repository.PlayerRepository
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


//@RunWith(MockitoJUnitRunner::class)
//internal class GetTeamSizeTests {
//
//    @Mock lateinit var playerDao: PlayerRepository
//    lateinit var mGetTeamSize: GetTeamSize
//    private var players = mutableListOf<Player>()
//    lateinit var team: Team
//
//    @Before
//    fun init() {
//        MockitoAnnotations.initMocks(this)
//        mGetTeamSize = GetTeamSize(playerDao)
//
//        Mockito.`when`(playerDao.getPlayers(1)).thenReturn(Single.just(players))
//        Mockito.`when`(playerDao.getPlayers(2)).thenReturn(Single.error(Exception()))
//
//        team = Team(1, "toto", "image", 0, false)
//
//        players.add(Player(1, 1, "", 1, 1, null, 1))
//        players.add(Player(2, 1, "", 2, 2, null, 1))
//        players.add(Player(3, 1, "", 3, 3, null, 1))
//        players.add(Player(4, 1, "", 4, 4, null, 1))
//        players.add(Player(5, 1, "", 5, 5, null, 1))
//    }
//
//    @Test
//    fun shouldTriggerAnExceptionIfTeamNotFound() {
//        val observer = TestObserver<GetTeamSize.ResponseValue>()
//        team.id = 2
//        mGetTeamSize.executeUseCase(GetTeamSize.RequestValues(team))
//                .subscribe(observer)
//        observer.await()
//        observer.assertError(Exception::class.java)
//    }
//
//    @Test
//    fun shouldReturnThePlayersSize() {
//        val observer = TestObserver<GetTeamSize.ResponseValue>()
//        mGetTeamSize.executeUseCase(GetTeamSize.RequestValues(team))
//                .subscribe(observer)
//        observer.await()
//        observer.assertComplete()
//        Assert.assertEquals(5, observer.values().first().data.getData()[KEY_DATA_SIZE] as? Int)
//        Assert.assertEquals("image", observer.values().first().data.getData()[KEY_DATA_TEAM_IMAGE] as? String)
//    }
//}