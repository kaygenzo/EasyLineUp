/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.usecases.SaveLineupChanges
import com.telen.easylineup.domain.usecases.SavePlayerNumberOverlay
import com.telen.easylineup.domain.usecases.UpdateLineup
import com.telen.easylineup.domain.usecases.UpdateLineupRoster
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
internal class SaveLineupChangesTests {
    @Mock
    lateinit var updateLineup: UpdateLineup

    @Mock
    lateinit var updateLineupRoster: UpdateLineupRoster

    @Mock
    lateinit var savePlayerNumberOverlay: SavePlayerNumberOverlay

    private val transactionRunner = TestTransactionRunner()
    private lateinit var saveLineupChanges: SaveLineupChanges
    private val lineup = Lineup(id = 1L, name = "lineup", teamId = 1L, tournamentId = 1L)
    private val roster = emptyList<RosterPlayerStatus>()

    @Before
    fun init() {
        saveLineupChanges = SaveLineupChanges(
            updateLineup,
            updateLineupRoster,
            savePlayerNumberOverlay,
            transactionRunner,
            testDispatcherProvider()
        )
    }

    @Test
    fun shouldSaveFieldsRosterAndNumbersInOneTransaction() = runTest {
        val inTransaction = mutableListOf<Boolean>()
        Mockito.`when`(updateLineup(lineup)).thenAnswer {
            inTransaction.add(transactionRunner.isInTransaction)
            Result.success(Unit)
        }
        Mockito.`when`(updateLineupRoster(lineup.id, roster)).thenAnswer {
            inTransaction.add(transactionRunner.isInTransaction)
            Result.success(Unit)
        }
        Mockito.`when`(savePlayerNumberOverlay(roster)).thenAnswer {
            inTransaction.add(transactionRunner.isInTransaction)
            Result.success(Unit)
        }

        assertTrue(saveLineupChanges(lineup, roster).isSuccess)
        assertEquals(listOf(true, true, true), inTransaction)
        assertEquals(1, transactionRunner.transactions)
    }

    @Test
    fun shouldStopAtAnInvalidLineupBeforeTouchingTheRoster() = runTest {
        Mockito.`when`(updateLineup(lineup))
            .thenReturn(Result.failure(LineupNameEmptyException()))

        val result = saveLineupChanges(lineup, roster)

        assertTrue(result.exceptionOrNull() is LineupNameEmptyException)
        verify(updateLineupRoster, never()).invoke(any(), any())
        verify(savePlayerNumberOverlay, never()).invoke(any())
    }

    @Test
    fun shouldFailWhenTheRosterCannotBeSaved() = runTest {
        Mockito.`when`(updateLineup(lineup)).thenReturn(Result.success(Unit))
        Mockito.`when`(updateLineupRoster(lineup.id, roster))
            .thenReturn(Result.failure(IllegalStateException("db error")))

        val result = saveLineupChanges(lineup, roster)

        assertTrue(result.exceptionOrNull() is IllegalStateException)
        verify(savePlayerNumberOverlay, never()).invoke(any())
    }
}
