/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.UpdateLineup
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class UpdateLineupTests {
    @Mock lateinit var lineupRepo: LineupRepository
    lateinit var updateLineup: UpdateLineup
    lateinit var lineup: Lineup

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        updateLineup = UpdateLineup(lineupRepo, testDispatcherProvider())
        lineup = Lineup(id = 1L, name = "test", teamId = 1L, tournamentId = 1L)
    }

    @Test
    fun shouldUpdateLineupThroughRepository() = runTest {
        Mockito.`when`(lineupRepo.updateLineup(lineup)).thenReturn(Unit)

        val result = updateLineup(lineup)

        assertTrue(result.isSuccess)
        verify(lineupRepo).updateLineup(lineup)
    }

    @Test
    fun shouldPropagateErrorFromRepository() = runTest {
        val exception = Exception("db error")
        Mockito.`when`(lineupRepo.updateLineup(lineup)).thenAnswer { throw exception }

        val result = updateLineup(lineup)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == exception.message)
    }
}
