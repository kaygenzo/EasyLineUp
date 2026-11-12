/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import org.mockito.kotlin.verify
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.usecases.UpdateLineup
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verifyNoInteractions

@RunWith(MockitoJUnitRunner::class)
internal class UpdateLineupTests {
    @Mock lateinit var lineupRepo: LineupRepository
    lateinit var updateLineup: UpdateLineup
    lateinit var lineup: Lineup

    @Before
    fun init() {
        updateLineup = UpdateLineup(lineupRepo, fixedClock(), testDispatcherProvider())
        lineup = Lineup(id = 1L, name = "test", teamId = 1L, tournamentId = 1L)
    }

    @Test
    fun shouldUpdateLineupThroughRepositoryStampedAsEditedNow() = runTest {
        val stamped = lineup.copy(editedTimeInMillis = TEST_NOW)
        Mockito.`when`(lineupRepo.updateLineup(stamped)).thenReturn(Unit)

        val result = updateLineup(lineup)

        assertTrue(result.isSuccess)
        verify(lineupRepo).updateLineup(stamped)
    }

    @Test
    fun shouldPropagateErrorFromRepository() = runTest {
        val exception = Exception("db error")
        Mockito.`when`(lineupRepo.updateLineup(any())).thenAnswer { throw exception }

        val result = updateLineup(lineup)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == exception.message)
    }

    @Test
    fun shouldFailWhenNameIsBlank() = runTest {
        val result = updateLineup(lineup.copy(name = "  "))

        assertTrue(result.exceptionOrNull() is LineupNameEmptyException)
        verifyNoInteractions(lineupRepo)
    }

    @Test
    fun shouldFailWhenTournamentIdIsNotPositive() = runTest {
        val result = updateLineup(lineup.copy(tournamentId = 0))

        assertTrue(result.exceptionOrNull() is TournamentNameEmptyException)
        verifyNoInteractions(lineupRepo)
    }
}
