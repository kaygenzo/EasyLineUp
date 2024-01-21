/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.usecases.GetListAvailablePlayersForSelection
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetListAvailablePlayersForSelectionTests : BaseUseCaseTests() {
    private val observer: TestObserver<GetListAvailablePlayersForSelection.ResponseValue> =
        TestObserver()
    lateinit var getListAvailablePlayersForSelection: GetListAvailablePlayersForSelection
    lateinit var players: MutableList<PlayerWithPosition>
    lateinit var roster: MutableList<RosterPlayerStatus>

    @Before
    fun init() {
        getListAvailablePlayersForSelection = GetListAvailablePlayersForSelection()

        players = mutableListOf(
            generate(1L, FieldPosition.PITCHER, PlayerFieldPosition.FLAG_NONE, 0, 1),
            generate(2L, null, PlayerFieldPosition.FLAG_NONE, 2, 2),
            generate(3L, FieldPosition.CENTER_FIELD, PlayerFieldPosition.FLAG_NONE, 9, 4),
            generate(4L, null, PlayerFieldPosition.FLAG_NONE, 10, 8),
            generate(
                5L,
                FieldPosition.SUBSTITUTE,
                PlayerFieldPosition.FLAG_NONE,
                Constants.SUBSTITUTE_ORDER_VALUE,
                16
            )
        )

        roster = mutableListOf(
            generateRosterPlayerStatus(1L, 1, true),
            generateRosterPlayerStatus(2L, 2, true),
            generateRosterPlayerStatus(3L, 4, true),
            generateRosterPlayerStatus(4L, 8, true),
            generateRosterPlayerStatus(5L, 16, true)
        )
    }

    private fun startUseCase(
        position: FieldPosition?,
        players: List<PlayerWithPosition> = this.players,
        roster: List<RosterPlayerStatus>? = this.roster,
        exception: Class<out Throwable>? = null
    ) {
        val request = GetListAvailablePlayersForSelection.RequestValues(players, position, roster)
        getListAvailablePlayersForSelection.executeUseCase(request).subscribe(observer)
        observer.await()
        exception?.let {
            observer.assertError(exception)
        } ?: let {
            observer.assertComplete()
        }
    }

    @Test
    fun shouldTriggerAnErrorIfListEmpty() {
        startUseCase(
            players = mutableListOf(),
            position = FieldPosition.PITCHER,
            exception = NoSuchElementException::class.java
        )
    }

    @Test
    fun shouldOnlyReturnPlayersWithoutFieldPositionOrSubstitutes() {
        startUseCase(position = FieldPosition.PITCHER)
        observer.values().first().players.let {
            Assert.assertEquals(3, it.size)
            Assert.assertEquals(1, it.filter { it.playerId == 2L }.size)
            Assert.assertEquals(1, it.filter { it.playerId == 4L }.size)
            Assert.assertEquals(1, it.filter { it.playerId == 5L }.size)
        }
    }

    @Test
    fun shouldSortPlayersByFieldPositionCatcher() {
        startUseCase(position = FieldPosition.CATCHER)
        observer.values().first().players.let {
            Assert.assertEquals(2, it[0].playerId)
            Assert.assertEquals(4, it[1].playerId)
        }
    }

    @Test
    fun shouldSortPlayersByFieldPositionSecondBase() {
        startUseCase(position = FieldPosition.SECOND_BASE)
        observer.values().first().players.let {
            Assert.assertEquals(4, it[0].playerId)
            Assert.assertEquals(2, it[1].playerId)
        }
    }

    @Test
    fun shouldReturnAllPlayersWhenRosterIsNull() {
        startUseCase(position = FieldPosition.SECOND_BASE, roster = null)
        observer.values().first().players.let {
            Assert.assertEquals(3, it.size)
        }
    }

    @Test
    fun shouldRtriggerAnExceptionWhenRosterIsEmpty() {
        startUseCase(
            roster = mutableListOf(),
            position = FieldPosition.SECOND_BASE,
            exception = NoSuchElementException::class.java
        )
    }

    @Test
    fun shouldReturnSomePlayersWhenRosterIsNotNullAndNotEmpty() {
        roster.removeIf { it.player.id == 4L || it.player.id == 5L }
        startUseCase(position = FieldPosition.SECOND_BASE)
        Assert.assertEquals(1, observer.values().first().players.size)
        Assert.assertEquals(2L, observer.values().first().players.first().playerId)
    }
}
