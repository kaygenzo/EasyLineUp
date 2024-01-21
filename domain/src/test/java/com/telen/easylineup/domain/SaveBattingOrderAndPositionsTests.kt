/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
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
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class SaveBattingOrderAndPositionsTests : BaseUseCaseTests() {
    private val observer: TestObserver<SaveBattingOrderAndPositions.ResponseValue> = TestObserver()
    private val lineup = Lineup(id = 1L, mode = MODE_DISABLED)

    @Mock
    private lateinit var playerFieldPositionRepository: PlayerFieldPositionRepository

    @Mock
    private lateinit var lineupRepository: LineupRepository
    private lateinit var saveBattingOrder: SaveBattingOrderAndPositions
    private lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {
        saveBattingOrder =
                SaveBattingOrderAndPositions(lineupRepository, playerFieldPositionRepository)
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

    fun startUseCase(exception: Class<out Throwable>? = null) {
        saveBattingOrder.executeUseCase(
            SaveBattingOrderAndPositions.RequestValues(
                lineup,
                players
            )
        ).subscribe(observer)
        observer.await()
        exception?.let {
            observer.assertError(exception)
        } ?: let {
            observer.assertComplete()
        }
    }

    @Test
    fun shouldTriggerAnErrorIfLineupIdEqualsZero() {
        lineup.id = 0L
        startUseCase(IllegalStateException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfLineupIdLessThanZero() {
        lineup.id = -1L
        startUseCase(IllegalStateException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfCannotSaveLineup() {
        Mockito.`when`(lineupRepository.updateLineup(any()))
            .thenReturn(Completable.error(IllegalStateException()))
        startUseCase(IllegalStateException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfCannotCreateAtLeastOnePlayer() {
        Mockito.`when`(playerFieldPositionRepository.insertPlayerFieldPosition(any()))
            .thenReturn(Single.error(IllegalStateException()))
        startUseCase(IllegalStateException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfCannotDeleteAtLeastOnePlayer() {
        Mockito.`when`(playerFieldPositionRepository.updatePlayerFieldPosition(any()))
            .thenReturn(Completable.error(IllegalStateException()))
        startUseCase(IllegalStateException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfCannotUpdateAtLeastOnePlayer() {
        Mockito.`when`(playerFieldPositionRepository.updatePlayerFieldPosition(any()))
            .thenReturn(Completable.error(IllegalStateException()))
        startUseCase(IllegalStateException::class.java)
    }

    @Test
    fun shouldInsertOnlyPlayersWithFieldIdZero() {
        startUseCase()
        verify(playerFieldPositionRepository)
            .insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
                Assert.assertEquals(2L, it.playerId)
            })
    }

    @Test
    fun shouldUpdateOnlyPlayersAssignedAndFieldIdGreaterThanZero() {
        startUseCase()
        verify(playerFieldPositionRepository, times(3))
            .updatePlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
                Assert.assertTrue(it.id == 1L || it.id == 3L || it.id == 4L)
            })
    }

    @Test
    fun shouldDeleteOnlyPlayersNotAssignedButWithFieldIdGreaterThanZero() {
        startUseCase()
        verify(playerFieldPositionRepository).deletePosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertTrue(it.id == 5L)
        })
    }
}
