/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.DeletePlayer
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerTests {
    val observer: TestObserver<DeletePlayer.ResponseValue> = TestObserver()

    @Mock
    lateinit var playerDao: PlayerRepository
    lateinit var deletePlayer: DeletePlayer
    private lateinit var player: Player

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        deletePlayer = DeletePlayer(playerDao)

        player = Player(
            id = 1L,
            teamId = 1L,
            name = "toto",
            shirtNumber = 1,
            licenseNumber = 1,
            image = null,
            positions = 1
        )

        Mockito.`when`(playerDao.deletePlayer(player)).thenReturn(Completable.complete())
    }

    @Test
    fun shouldDeletePlayer() {
        deletePlayer.executeUseCase(DeletePlayer.RequestValues(player)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerDao).deletePlayer(player)
    }
}
