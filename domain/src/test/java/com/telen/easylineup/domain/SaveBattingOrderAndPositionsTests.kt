/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.SaveBattingOrderAndPositions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class SaveBattingOrderAndPositionsTests : BaseUseCaseTests() {
    private val lineup = Lineup(id = 1L, mode = MODE_DISABLED)

    @Mock
    private lateinit var playerFieldPositionRepository: PlayerFieldPositionRepository

    @Mock
    private lateinit var lineupRepository: LineupRepository
    private lateinit var saveBattingOrder: SaveBattingOrderAndPositions
    private lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {
        saveBattingOrder = SaveBattingOrderAndPositions(
            lineupRepository, playerFieldPositionRepository, testDispatcherProvider()
        )
        Mockito.`when`(lineupRepository.updateLineup(lineup)).thenReturn(Completable.complete())
        Mockito.`when`(playerFieldPositionRepository.updatePlayerFieldPosition(any()))
            .thenReturn(Completable.complete())
        Mockito.`when`(playerFieldPositionRepository.insertPlayerFieldPosition(any()))
            .thenReturn(Single.just(1L))
        Mockito.`when`(playerFieldPositionRepository.deletePosition(any()))
            .thenReturn(Completable.complete())
        players = mutableListOf(
            /* old position still acquired */
            generate(1L, FieldPosition.FIRST_BASE, 0, 1),
            /* new position freshly assigned */
            generate(2L, FieldPosition.SECOND_BASE, 0, 1)
                .apply { fieldPositionId = 0L },
            generate(3L, FieldPosition.SUBSTITUTE, 0, 1),
            generate(4L, FieldPosition.OLD_SUBSTITUTE, 0, 1),
            /* old position released */
            generate(5L, null, 0, 1),
        )
    }

    suspend fun startUseCase(exception: Class<out Throwable>? = null) {
        val result = saveBattingOrder(lineup, players)
        exception?.let {
            assertTrue(exception.isInstance(result.exceptionOrNull()))
        } ?: let {
            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun shouldTriggerAnErrorIfLineupIdEqualsZero() = runTest {
        lineup.id = 0L
        startUseCase(IllegalStateException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfLineupIdLessThanZero() = runTest {
        lineup.id = -1L
        startUseCase(IllegalStateException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfCannotSaveLineup() = runTest {
        Mockito.`when`(lineupRepository.updateLineup(any()))
            .thenReturn(Completable.error(IllegalStateException()))
        startUseCase(IllegalStateException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfCannotCreateAtLeastOnePlayer() = runTest {
        Mockito.`when`(playerFieldPositionRepository.insertPlayerFieldPosition(any()))
            .thenReturn(Single.error(IllegalStateException()))
        startUseCase(IllegalStateException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfCannotDeleteAtLeastOnePlayer() = runTest {
        Mockito.`when`(playerFieldPositionRepository.deletePosition(any()))
            .thenReturn(Completable.error(IllegalStateException()))
        startUseCase(IllegalStateException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfCannotUpdateAtLeastOnePlayer() = runTest {
        Mockito.`when`(playerFieldPositionRepository.updatePlayerFieldPosition(any()))
            .thenReturn(Completable.error(IllegalStateException()))
        startUseCase(IllegalStateException::class.java)
    }

    @Test
    fun shouldInsertOnlyPlayersWithFieldIdZeroAndAssigned() = runTest {
        players.add(generate(0L, FieldPosition.OLD_SUBSTITUTE, 0, 0).apply {
            position = 0
            playerId = 100L
        })
        startUseCase()
        verify(playerFieldPositionRepository).insertPlayerFieldPosition(argThat { playerId == 2L })
        verify(playerFieldPositionRepository, never())
            .insertPlayerFieldPosition(argThat { playerId == 100L })
    }

    @Test
    fun shouldUpdateOnlyPlayersAssignedAndFieldIdGreaterThanZero() = runTest {
        startUseCase()
        verify(playerFieldPositionRepository, times(3))
            .updatePlayerFieldPosition(check {
                Assert.assertTrue(it.id == 1L || it.id == 3L || it.id == 4L)
            })
    }

    @Test
    fun shouldDeleteOnlyPlayersNotAssignedButWithFieldIdGreaterThanZero() = runTest {
        startUseCase()
        verify(playerFieldPositionRepository).deletePosition(check {
            Assert.assertTrue(it.id == 5L)
        })
    }
}
