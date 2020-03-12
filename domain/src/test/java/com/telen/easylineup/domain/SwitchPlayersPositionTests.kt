package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
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
class SwitchPlayersPositionTests {

    @Mock lateinit var playerFieldPositionsDao: PlayerFieldPositionsDao
    lateinit var mSwitchPlayersPosition: SwitchPlayersPosition

    private lateinit var player1: PlayerWithPosition
    private lateinit var player2: PlayerWithPosition
    private lateinit var player2bis: PlayerWithPosition

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mSwitchPlayersPosition = SwitchPlayersPosition(playerFieldPositionsDao)

        player1 = PlayerWithPosition(
                "toto", 1, 1, 1,
                null, 1, 0f,0f, 1, 1,
                1, 1, 1)

        player2 = PlayerWithPosition(
                "tata", 2, 2, 1,
                null, 2, 0f,0f, 2, 2,
                2, 1, 1)

        player2bis = PlayerWithPosition(
                "tata", 2, 2, 1,
                null, 2, 0f,0f, 2, 2,
                1, 1, 1)

        Mockito.`when`(playerFieldPositionsDao.updatePlayerFieldPositions(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerSamePlayersException() {
        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(player1, player2bis)).subscribe(observer)
        observer.await()
        observer.assertError(SamePlayerException::class.java)
    }

    @Test
    fun shouldSwitchPlayerPositions() {
        val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()
        mSwitchPlayersPosition.executeUseCase(SwitchPlayersPosition.RequestValues(player1, player2)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it[0].id)
            Assert.assertEquals(1, it[0].playerId)
            Assert.assertEquals(2, it[0].position)

            Assert.assertEquals(2, it[1].id)
            Assert.assertEquals(2, it[1].playerId)
            Assert.assertEquals(1, it[1].position)
        })
    }
}