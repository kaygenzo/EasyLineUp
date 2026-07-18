/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import androidx.lifecycle.MutableLiveData
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.ObservePlayer
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class ObservePlayerTests {
    @Mock lateinit var playerDao: PlayerRepository
    lateinit var observePlayer: ObservePlayer

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        observePlayer = ObservePlayer(playerDao)
    }

    @Test
    fun shouldDelegateToRepository() {
        val liveData = MutableLiveData<Player>()
        Mockito.`when`(playerDao.getPlayerById(1L)).thenReturn(liveData)

        assertSame(liveData, observePlayer(1L))
    }
}
