/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.usecases.SaveBattingOrderAndPositions
import com.telen.easylineup.domain.usecases.SaveLineupDraft
import com.telen.easylineup.domain.usecases.SavePlayerNumberOverlay
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
internal class SaveLineupDraftTests {
    @Mock
    lateinit var saveBattingOrderAndPositions: SaveBattingOrderAndPositions

    @Mock
    lateinit var savePlayerNumberOverlay: SavePlayerNumberOverlay

    private val transactionRunner = TestTransactionRunner()
    private lateinit var saveLineupDraft: SaveLineupDraft
    private val lineup = Lineup(id = 1L, name = "Game 1", teamId = 1L, tournamentId = 1L)
    private val players = emptyList<PlayerWithPosition>()
    private val numbers = emptyList<RosterPlayerStatus>()

    @Before
    fun init() {
        saveLineupDraft = SaveLineupDraft(
            saveBattingOrderAndPositions,
            savePlayerNumberOverlay,
            transactionRunner,
            testDispatcherProvider()
        )
    }

    @Test
    fun shouldSavePositionsAndNumbersInOneTransaction() = runTest {
        val inTransaction = mutableListOf<Boolean>()
        Mockito.`when`(saveBattingOrderAndPositions(lineup, players)).thenAnswer {
            inTransaction.add(transactionRunner.isInTransaction)
            Result.success(Unit)
        }
        Mockito.`when`(savePlayerNumberOverlay(numbers)).thenAnswer {
            inTransaction.add(transactionRunner.isInTransaction)
            Result.success(Unit)
        }

        assertTrue(saveLineupDraft(lineup, players, numbers).isSuccess)
        assertEquals(listOf(true, true), inTransaction)
        assertEquals(1, transactionRunner.transactions)
        assertEquals(0, transactionRunner.rollbacks)
    }

    @Test
    fun shouldRollBackAndSkipTheNumbersWhenThePositionsCannotBeSaved() = runTest {
        Mockito.`when`(saveBattingOrderAndPositions(lineup, players))
            .thenReturn(Result.failure(IllegalStateException("db error")))

        val result = saveLineupDraft(lineup, players, numbers)

        assertTrue(result.exceptionOrNull() is IllegalStateException)
        verify(savePlayerNumberOverlay, never()).invoke(any())
        assertEquals(1, transactionRunner.rollbacks)
    }

    @Test
    fun shouldRollBackThePositionsWhenTheNumbersCannotBeSaved() = runTest {
        Mockito.`when`(saveBattingOrderAndPositions(lineup, players))
            .thenReturn(Result.success(Unit))
        Mockito.`when`(savePlayerNumberOverlay(numbers))
            .thenReturn(Result.failure(IllegalStateException("db error")))

        val result = saveLineupDraft(lineup, players, numbers)

        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals(1, transactionRunner.rollbacks)
    }
}
