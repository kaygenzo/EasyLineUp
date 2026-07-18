/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.InsertPlayers
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
internal class InsertPlayersTests {
    @Mock lateinit var playerDao: PlayerRepository
    lateinit var insertPlayers: InsertPlayers

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        insertPlayers = InsertPlayers(playerDao, testSchedulersProvider())
    }

    @Test
    fun shouldDelegateToRepository() {
        val players = listOf(
            Player(id = 1L, teamId = 1L, name = "Toto", shirtNumber = 1, licenseNumber = 1L)
        )
        Mockito.`when`(playerDao.insertPlayers(players)).thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        insertPlayers(players).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(playerDao).insertPlayers(players)
    }
}
