/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.DeletePlayerFieldPosition
import com.telen.easylineup.domain.usecases.SaveBattingOrderAndPositions
import com.telen.easylineup.domain.usecases.UpdateLineupRoster
import kotlinx.coroutines.runBlocking
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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
internal class UpdateLineupRosterTests {
    @Mock lateinit var lineupRepository: LineupRepository
    @Mock lateinit var pfpRepository: PlayerFieldPositionRepository
    lateinit var updateLineupRoster: UpdateLineupRoster
    lateinit var lineup: Lineup

    private fun player(id: Long) =
        Player(id = id, teamId = 1L, name = "p$id", shirtNumber = id.toInt(), licenseNumber = id.toString())

    private fun positioned(id: Long, position: FieldPosition, order: Int) = PlayerWithPosition(
        playerName = "p$id",
        playerSex = 0,
        shirtNumber = id.toInt(),
        licenseNumber = id.toString(),
        teamId = 1L,
        image = null,
        position = position.id,
        order = order,
        fieldPositionId = id * 10,
        playerId = id,
        lineupId = 1L,
        playerPositions = 0
    )

    private fun roster(vararg selected: Pair<Long, Boolean>) = selected.map { (id, status) ->
        RosterPlayerStatus(player(id), status = status, playerNumberOverlay = null)
    }

    @Before
    fun init() {
        runBlocking {
            val dispatchers = testDispatcherProvider()
            updateLineupRoster = UpdateLineupRoster(
                lineupRepository,
                pfpRepository,
                DeletePlayerFieldPosition(dispatchers),
                SaveBattingOrderAndPositions(lineupRepository, pfpRepository, dispatchers),
                dispatchers
            )
            lineup = Lineup(id = 1L, name = "test", teamId = 1L, tournamentId = 1L)
            Mockito.`when`(lineupRepository.getLineupById(1L)).thenReturn(lineup)
            Mockito.lenient().`when`(lineupRepository.updateLineup(any())).thenReturn(Unit)
            Mockito.lenient().`when`(pfpRepository.getAllPlayersWithPositionsForLineup(1L))
                .thenReturn(emptyList())
        }
    }

    @Test
    fun shouldBuildRosterStringFromSelectedPlayersOnly() = runTest {
        val result = updateLineupRoster(1L, roster(1L to true, 2L to false, 3L to true))

        assertTrue(result.isSuccess)
        val captor = argumentCaptor<Lineup>()
        verify(lineupRepository).updateLineup(captor.capture())
        assertEquals("1;3", captor.firstValue.roster)
    }

    @Test
    fun shouldBuildEmptyRosterStringWhenNoPlayerSelected() = runTest {
        val result = updateLineupRoster(1L, roster(1L to false))

        assertTrue(result.isSuccess)
        val captor = argumentCaptor<Lineup>()
        verify(lineupRepository).updateLineup(captor.capture())
        assertEquals("", captor.firstValue.roster)
    }

    @Test
    fun shouldLeavePositionsUntouchedWhenNoRemovedPlayerIsInTheLineup() = runTest {
        Mockito.`when`(pfpRepository.getAllPlayersWithPositionsForLineup(1L))
            .thenReturn(listOf(positioned(1L, FieldPosition.PITCHER, 1)))

        val result = updateLineupRoster(1L, roster(1L to true, 2L to false))

        assertTrue(result.isSuccess)
        verify(pfpRepository, never()).deletePosition(any())
        verify(pfpRepository, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldRemoveFieldPlayerTakenOffTheRoster() = runTest {
        val pitcher = positioned(1L, FieldPosition.PITCHER, 1)
        val catcher = positioned(2L, FieldPosition.CATCHER, 2)
        Mockito.`when`(pfpRepository.getAllPlayersWithPositionsForLineup(1L))
            .thenReturn(listOf(pitcher, catcher))

        val result = updateLineupRoster(1L, roster(1L to true, 2L to false))

        assertTrue(result.isSuccess)
        val deleted = argumentCaptor<PlayerFieldPosition>()
        verify(pfpRepository).deletePosition(deleted.capture())
        assertEquals(20L, deleted.firstValue.id)
        assertEquals(2L, deleted.firstValue.playerId)
        val savedLineup = argumentCaptor<Lineup>()
        verify(lineupRepository).updateLineup(savedLineup.capture())
        assertEquals("1", savedLineup.firstValue.roster)
    }

    @Test
    fun shouldHandBatterSlotOfRemovedSubstituteToNextSubstitute() = runTest {
        Mockito.`when`(lineupRepository.getLineupById(1L))
            .thenReturn(lineup.copy(extraHitters = 1))
        val battingSub = positioned(1L, FieldPosition.SUBSTITUTE, 10)
        val benchSub = positioned(2L, FieldPosition.SUBSTITUTE, Constants.SUBSTITUTE_ORDER_VALUE)
        Mockito.`when`(pfpRepository.getAllPlayersWithPositionsForLineup(1L))
            .thenReturn(listOf(battingSub, benchSub))

        val result = updateLineupRoster(1L, roster(1L to false, 2L to true))

        assertTrue(result.isSuccess)
        verify(pfpRepository).deletePosition(check { assertEquals(10L, it.id) })
        verify(pfpRepository).updatePlayerFieldPosition(
            check {
                assertEquals(20L, it.id)
                assertEquals(10, it.order)
            }
        )
    }

    @Test
    fun shouldPropagateErrorWhenLineupNotFound() = runTest {
        val exception = Exception("not found")
        Mockito.`when`(lineupRepository.getLineupById(eq(1L))).thenAnswer { throw exception }

        val result = updateLineupRoster(1L, emptyList())

        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }
}
