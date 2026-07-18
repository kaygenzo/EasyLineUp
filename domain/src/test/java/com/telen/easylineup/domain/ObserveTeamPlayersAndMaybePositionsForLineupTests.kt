/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.ObserveTeamPlayersAndMaybePositionsForLineup
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
internal class ObserveTeamPlayersAndMaybePositionsForLineupTests {
    @Mock lateinit var playerDao: PlayerRepository
    lateinit var observeTeamPlayersAndMaybePositionsForLineup:
        ObserveTeamPlayersAndMaybePositionsForLineup

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        observeTeamPlayersAndMaybePositionsForLineup =
            ObserveTeamPlayersAndMaybePositionsForLineup(playerDao)
    }

    @Test
    fun shouldDelegateToRepository() {
        val flowable = Flowable.just(listOf<PlayerWithPosition>())
        Mockito.`when`(playerDao.getTeamPlayersAndMaybePositions(1L)).thenReturn(flowable)

        assertSame(flowable, observeTeamPlayersAndMaybePositionsForLineup(1L))
    }
}
