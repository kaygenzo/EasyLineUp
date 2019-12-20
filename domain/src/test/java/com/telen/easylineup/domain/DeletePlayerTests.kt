package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.model.Player
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class DeletePlayerTests {

    @Mock lateinit var playerDao: PlayerDao
    lateinit var mDeletePlayer: DeletePlayer

    private lateinit var player: Player

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mDeletePlayer = DeletePlayer(playerDao)

        player = Player(id = 1L, teamId = 1L, name = "toto", shirtNumber =  1, licenseNumber = 1, image = null, positions = 1)

        Mockito.`when`(playerDao.deletePlayer(player)).thenReturn(Completable.complete())
    }

    @Test
    fun shouldDeletePlayer() {
        val observer = TestObserver<DeletePlayer.ResponseValue>()
        mDeletePlayer.executeUseCase(DeletePlayer.RequestValues(player)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerDao).deletePlayer(player)
    }
}