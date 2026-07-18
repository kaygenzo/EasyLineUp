/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.InsertPlayerNumberOverlays
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
internal class InsertPlayerNumberOverlaysTests {
    @Mock lateinit var playerDao: PlayerRepository
    lateinit var insertPlayerNumberOverlays: InsertPlayerNumberOverlays

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        insertPlayerNumberOverlays = InsertPlayerNumberOverlays(playerDao)
    }

    @Test
    fun shouldDelegateToRepository() {
        val overlays = listOf(PlayerNumberOverlay(id = 1L, lineupId = 10L, playerId = 1L, number = 8))
        Mockito.`when`(playerDao.createPlayerNumberOverlays(overlays)).thenReturn(Completable.complete())

        val observer = TestObserver<InsertPlayerNumberOverlays.ResponseValue>()
        insertPlayerNumberOverlays
            .executeUseCase(InsertPlayerNumberOverlays.RequestValues(overlays))
            .subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(playerDao).createPlayerNumberOverlays(overlays)
    }
}
