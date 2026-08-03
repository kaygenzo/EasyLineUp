/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.ports.repository.LineupRepository
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

@RunWith(MockitoJUnitRunner::class)
internal class UpdateLineupRosterTests {
    @Mock lateinit var lineupRepository: LineupRepository
    lateinit var updateLineupRoster: UpdateLineupRoster
    lateinit var lineup: Lineup

    private fun player(id: Long) = Player(id = id, teamId = 1L, name = "p$id", shirtNumber = id.toInt(), licenseNumber = id)

    @Before
    fun init() {
        runBlocking {
        updateLineupRoster = UpdateLineupRoster(lineupRepository, testDispatcherProvider())
        lineup = Lineup(id = 1L, name = "test", teamId = 1L, tournamentId = 1L)
        Mockito.`when`(lineupRepository.getLineupById(1L)).thenReturn(lineup)
        Mockito.`when`(lineupRepository.updateLineup(any())).thenReturn(Unit)
    }
    }

    @Test
    fun shouldBuildRosterStringFromSelectedPlayersOnly() = runTest {
        val roster = listOf(
            RosterPlayerStatus(player(1L), status = true, playerNumberOverlay = null),
            RosterPlayerStatus(player(2L), status = false, playerNumberOverlay = null),
            RosterPlayerStatus(player(3L), status = true, playerNumberOverlay = null)
        )

        val result = updateLineupRoster(1L, roster)

        assertTrue(result.isSuccess)
        val captor = argumentCaptor<Lineup>()
        Mockito.verify(lineupRepository).updateLineup(captor.capture())
        assertEquals("1;3", captor.firstValue.roster)
    }

    @Test
    fun shouldBuildEmptyRosterStringWhenNoPlayerSelected() = runTest {
        val roster = listOf(
            RosterPlayerStatus(player(1L), status = false, playerNumberOverlay = null)
        )

        val result = updateLineupRoster(1L, roster)

        assertTrue(result.isSuccess)
        val captor = argumentCaptor<Lineup>()
        Mockito.verify(lineupRepository).updateLineup(captor.capture())
        assertEquals("", captor.firstValue.roster)
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
