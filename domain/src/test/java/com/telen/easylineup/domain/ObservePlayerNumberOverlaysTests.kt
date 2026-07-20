/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.ObservePlayerNumberOverlays
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class ObservePlayerNumberOverlaysTests {
    @Mock lateinit var playerDao: PlayerRepository
    lateinit var observePlayerNumberOverlays: ObservePlayerNumberOverlays

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        observePlayerNumberOverlays = ObservePlayerNumberOverlays(playerDao)
    }

    @Test
    fun shouldDelegateToRepository() = runTest {
        val overlays = listOf<PlayerNumberOverlay>()
        Mockito.`when`(playerDao.observePlayersNumberOverlay(10L)).thenReturn(Flowable.just(overlays))

        val result = observePlayerNumberOverlays(10L).toList()

        assertEquals(listOf(overlays), result)
    }
}
