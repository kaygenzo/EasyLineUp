/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import androidx.lifecycle.MutableLiveData
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.ObservePlayerNumberOverlays
import org.junit.Assert.assertSame
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
    fun shouldDelegateToRepository() {
        val liveData = MutableLiveData<List<PlayerNumberOverlay>>()
        Mockito.`when`(playerDao.observePlayersNumberOverlay(10L)).thenReturn(liveData)

        assertSame(liveData, observePlayerNumberOverlays(10L))
    }
}
