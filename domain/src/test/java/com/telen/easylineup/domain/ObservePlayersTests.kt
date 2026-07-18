/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.ObservePlayers
import io.reactivex.rxjava3.core.Flowable
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class ObservePlayersTests {
    @Mock lateinit var playerDao: PlayerRepository
    lateinit var observePlayers: ObservePlayers

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        observePlayers = ObservePlayers(playerDao)
    }

    @Test
    fun shouldDelegateToRepository() {
        val flowable = Flowable.just(listOf<Player>())
        Mockito.`when`(playerDao.observePlayers(1L)).thenReturn(flowable)

        assertSame(flowable, observePlayers(1L))
    }
}
