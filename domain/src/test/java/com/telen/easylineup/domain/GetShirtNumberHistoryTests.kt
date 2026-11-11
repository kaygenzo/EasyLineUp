/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.repository.PlayerNumberOverlayRepository
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetShirtNumberHistory
import com.telen.easylineup.domain.usecases.GetTeam
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetShirtNumberHistoryTests {
    @Mock lateinit var playerRepo: PlayerRepository
    @Mock lateinit var playerNumberOverlayRepo: PlayerNumberOverlayRepository
    @Mock lateinit var teamRepo: TeamRepository
    lateinit var getShirtNumberEntry: GetShirtNumberHistory
    lateinit var entry1: ShirtNumberEntry
    lateinit var entry2: ShirtNumberEntry
    lateinit var entry3: ShirtNumberEntry
    lateinit var entry4: ShirtNumberEntry
    lateinit var entry5: ShirtNumberEntry
    lateinit var overlay1: PlayerNumberOverlay
    lateinit var overlay2: PlayerNumberOverlay
    lateinit var shirtNumberOverlay1: ShirtNumberEntry
    lateinit var shirtNumberOverlay2: ShirtNumberEntry

    @Before
    fun init() {
        runBlocking {
        getShirtNumberEntry = GetShirtNumberHistory(
            playersRepo = playerRepo,
            playerNumberOverlayRepo = playerNumberOverlayRepo,
            getTeam = GetTeam(teamRepo, testDispatcherProvider()),
            dispatcherProvider = testDispatcherProvider()
        )
        Mockito.`when`(teamRepo.getTeams())
            .thenReturn(listOf(Team(id = 1L, name = "Panthers", main = true)))

        entry1 = ShirtNumberEntry(1, "toto", 1L, 1L, 1L, 1L, "lineup1")
        entry4 = ShirtNumberEntry(1, "tutu", 2L, 2L, 1L, 1L, "lineup1")

        entry2 = ShirtNumberEntry(1, "toto", 1L, 4L, 2L, 2L, "lineup2")
        entry3 = ShirtNumberEntry(1, "tata", 2L, 5L, 2L, 2L, "lineup2")
        entry5 = ShirtNumberEntry(42, "titi", 3L, 6L, 2L, 3L, "lineup3")
        overlay1 = PlayerNumberOverlay(1, 2L, 3L, 1, "hash")
        overlay2 = PlayerNumberOverlay(2, 3L, 3L, 1, "hash")

        shirtNumberOverlay1 = ShirtNumberEntry(1, "titi", 3L, 6L, 2L, 2L, "lineup2")
        shirtNumberOverlay2 = ShirtNumberEntry(1, "test", 3L, 7L, 2L, 3L, "lineup3")

        Mockito.lenient().`when`(playerNumberOverlayRepo.getShirtNumberOverlay(1L, 1L))
            .thenReturn(null)
        Mockito.lenient().`when`(playerNumberOverlayRepo.getShirtNumberOverlay(2L, 1L))
            .thenReturn(null)
        Mockito.lenient().`when`(playerNumberOverlayRepo.getShirtNumberOverlay(1L, 2L))
            .thenReturn(null)
        Mockito.lenient().`when`(playerNumberOverlayRepo.getShirtNumberOverlay(2L, 2L))
            .thenReturn(null)
        Mockito.lenient().`when`(playerNumberOverlayRepo.getShirtNumberOverlay(3L, 3L))
            .thenReturn(null)

        Mockito.`when`(playerNumberOverlayRepo.getShirtNumberFromNumberOverlays(1L, 1))
            .thenReturn(listOf())
    }
    }

    @Test
    fun shouldGetAllShirtNumberFromPositions() = runTest {
        Mockito.`when`(playerRepo.getShirtNumberFromPlayers(1L, 1))
            .thenReturn(listOf(entry1, entry2, entry3, entry4))
        val result = getShirtNumberEntry(1)
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(listOf(entry3, entry2, entry4, entry1), result.getOrNull())
    }

    @Test
    fun shouldGetAllShirtNumberFromPositionsAndOverlays() = runTest {
        Mockito.`when`(playerRepo.getShirtNumberFromPlayers(1L, 1))
            .thenReturn(listOf(entry1, entry2, entry3, entry4))

        Mockito.`when`(playerNumberOverlayRepo.getShirtNumberFromNumberOverlays(1L, 1))
            .thenReturn(listOf(
            shirtNumberOverlay1, shirtNumberOverlay2
        ))

        val result = getShirtNumberEntry(1)
        Assert.assertTrue(result.isSuccess)
        val expected = listOf(
            shirtNumberOverlay2,
            shirtNumberOverlay1,
            entry3,
            entry2,
            entry4,
            entry1
        )
        Assert.assertEquals(expected, result.getOrNull())
    }

    @Test
    fun shouldNotGetPlayerNumberIfOverlayExists() = runTest {
        Mockito.`when`(playerRepo.getShirtNumberFromPlayers(1L, 42)).thenReturn(listOf(entry5))
        Mockito.`when`(playerNumberOverlayRepo.getShirtNumberOverlay(3L, 3L)).thenReturn(overlay2)
        Mockito.`when`(playerNumberOverlayRepo.getShirtNumberFromNumberOverlays(1L, 42))
            .thenReturn(listOf())

        val result = getShirtNumberEntry(42)
        Assert.assertTrue(result.isSuccess)
        val expected: List<ShirtNumberEntry> = listOf()
        Assert.assertEquals(expected, result.getOrNull())
    }

    private fun overlayEntry(playerId: Long, lineupId: Long, time: Long = 1L) =
        ShirtNumberEntry(1, "overlay", playerId, time, 1L, lineupId, "lineup$lineupId")

    @Test
    fun shouldAddAnOverlayFromALineupWhereTheNumberIsNotThePlayersOwn() = runTest {
        val overlay = overlayEntry(playerId = 9L, lineupId = 9L)
        Mockito.`when`(playerRepo.getShirtNumberFromPlayers(1L, 1)).thenReturn(emptyList())
        Mockito.`when`(playerNumberOverlayRepo.getShirtNumberFromNumberOverlays(1L, 1))
            .thenReturn(listOf(overlay))

        assertEquals(listOf(overlay), getShirtNumberEntry(1).getOrThrow())
    }

    @Test
    fun shouldNotListTheSameOverlayTwice() = runTest {
        Mockito.`when`(playerRepo.getShirtNumberFromPlayers(1L, 1)).thenReturn(listOf(entry2))
        Mockito.`when`(playerNumberOverlayRepo.getShirtNumberOverlay(1L, 2L))
            .thenReturn(PlayerNumberOverlay(1L, 2L, 1L, 1, "hash"))
        Mockito.`when`(playerNumberOverlayRepo.getShirtNumberFromNumberOverlays(1L, 1))
            .thenReturn(listOf(overlayEntry(playerId = 1L, lineupId = 2L)))

        val result = getShirtNumberEntry(1).getOrThrow()

        assertEquals(1, result.size)
        assertEquals("toto", result.single().playerName)
    }

    @Test
    fun shouldKeepOverlaysOfTheSamePlayerInAnotherLineup() = runTest {
        val otherLineup = overlayEntry(playerId = 1L, lineupId = 9L)
        Mockito.`when`(playerRepo.getShirtNumberFromPlayers(1L, 1)).thenReturn(listOf(entry2))
        Mockito.`when`(playerNumberOverlayRepo.getShirtNumberOverlay(1L, 2L))
            .thenReturn(PlayerNumberOverlay(1L, 2L, 1L, 1, "hash"))
        Mockito.`when`(playerNumberOverlayRepo.getShirtNumberFromNumberOverlays(1L, 1))
            .thenReturn(listOf(otherLineup))

        assertEquals(2, getShirtNumberEntry(1).getOrThrow().size)
    }

    @Test
    fun shouldKeepOverlaysOfAnotherPlayerInTheSameLineup() = runTest {
        val otherPlayer = overlayEntry(playerId = 9L, lineupId = 2L)
        Mockito.`when`(playerRepo.getShirtNumberFromPlayers(1L, 1)).thenReturn(listOf(entry2))
        Mockito.`when`(playerNumberOverlayRepo.getShirtNumberOverlay(1L, 2L))
            .thenReturn(PlayerNumberOverlay(1L, 2L, 1L, 1, "hash"))
        Mockito.`when`(playerNumberOverlayRepo.getShirtNumberFromNumberOverlays(1L, 1))
            .thenReturn(listOf(otherPlayer))

        assertEquals(2, getShirtNumberEntry(1).getOrThrow().size)
    }

    @Test
    fun shouldDropAPlayerWearingAnotherNumberInThatLineup() = runTest {
        Mockito.`when`(playerRepo.getShirtNumberFromPlayers(1L, 1)).thenReturn(listOf(entry2))
        Mockito.`when`(playerNumberOverlayRepo.getShirtNumberOverlay(1L, 2L))
            .thenReturn(PlayerNumberOverlay(1L, 2L, 1L, 42, "hash"))

        assertTrue(getShirtNumberEntry(1).getOrThrow().isEmpty())
    }

    @Test
    fun shouldSortByCreationDateWhenALineupHasNoEventTime() = runTest {
        val noEventTime = ShirtNumberEntry(1, "new", 1L, 0L, 9L, 1L, "lineup1")
        val withEventTime = ShirtNumberEntry(1, "old", 2L, 5L, 1L, 2L, "lineup2")
        Mockito.`when`(playerRepo.getShirtNumberFromPlayers(1L, 1))
            .thenReturn(listOf(withEventTime, noEventTime))

        assertEquals(listOf(noEventTime, withEventTime), getShirtNumberEntry(1).getOrThrow())
    }
}
