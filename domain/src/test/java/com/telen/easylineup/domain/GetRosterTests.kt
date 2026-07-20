/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetTeam
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetRosterTests {
    private val extraHitters = 0
    private val team = Team(id = 1L, name = "toto", main = true)
    private val lineup = Lineup(1L, "A", 1L, 1L,
        1, TeamStrategy.STANDARD.id, extraHitters, 3L, 1L, 1L, null, "hash")
    private val player1 = Player(1L, 1L, "A", 1,
        1, null, 1, 0, 0, "hash")
    private val player2 = Player(2L, 1L, "B", 2,
        2, null, 1, 0, 0, "hash")
    private val player3 = Player(3L, 1L, "C", 3,
        3, null, 1, 0, 0, "hash")
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getRoster: GetRoster

    @Before
    fun init() {
        runBlocking {
        MockitoAnnotations.initMocks(this)
        getRoster = GetRoster(playerDao, lineupDao, GetTeam(teamDao, testDispatcherProvider()), testDispatcherProvider())

        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(listOf(team))
        Mockito.`when`(lineupDao.getLineupByIdSingle(1L)).thenReturn(lineup)
        Mockito.`when`(playerDao.getPlayersByTeamId(1L)).thenReturn(listOf(player1, player2, player3))

        val overlays: MutableList<PlayerNumberOverlay> =
            mutableListOf<PlayerNumberOverlay>().apply {
                add(PlayerNumberOverlay(1L, 1L, player1.id, 42))
                add(PlayerNumberOverlay(3L, 1L, player3.id, 69))
            }

        Mockito.`when`(playerDao.getPlayersNumberOverlay(1L)).thenReturn(overlays)
    }
    }

    @Test
    fun shouldReturnAllPlayersIfLineupIdIsNull() = runTest {
        val result = getRoster(null)
        assertTrue(result.isSuccess)
        Assert.assertEquals(3, result.getOrNull()?.players?.filter { it.status }?.size)
        Assert.assertEquals(Constants.STATUS_ALL, result.getOrNull()?.status)
    }

    @Test
    fun shouldReturnAllPlayersIfLineupRosterIsNull() = runTest {
        lineup.roster = null
        val result = getRoster(1L)
        assertTrue(result.isSuccess)
        Assert.assertEquals(3, result.getOrNull()?.players?.filter { it.status }?.size)
        Assert.assertEquals(Constants.STATUS_ALL, result.getOrNull()?.status)
    }

    @Test
    fun shouldReturnNoPlayersIfLineupRosterIsEmpty() = runTest {
        lineup.roster = ""
        val result = getRoster(1L)
        assertTrue(result.isSuccess)
        Assert.assertEquals(0, result.getOrNull()?.players?.filter { it.status }?.size)
        Assert.assertEquals(Constants.STATUS_PARTIAL, result.getOrNull()?.status)
    }

    @Test
    fun shouldReturnAllPlayersIfLineupRosterIsFull() = runTest {
        lineup.roster = "1;2;3"
        val result = getRoster(1L)
        assertTrue(result.isSuccess)
        Assert.assertEquals(3, result.getOrNull()?.players?.filter { it.status }?.size)
        Assert.assertEquals(Constants.STATUS_ALL, result.getOrNull()?.status)
    }

    @Test
    fun shouldReturn2PlayersInRosterSelection() = runTest {
        lineup.roster = "1;3"
        val result = getRoster(1L)
        assertTrue(result.isSuccess)
        val players = result.getOrNull()?.players.orEmpty()
        Assert.assertEquals(2, players.filter { it.status }.size)
        Assert.assertEquals(false, players.first { it.player.id == 2L }.status)
        Assert.assertEquals(Constants.STATUS_PARTIAL, result.getOrNull()?.status)
    }

    @Test
    fun shouldReturnOverlaysNumber() = runTest {
        lineup.roster = "1;2;3"
        val result = getRoster(1L)
        assertTrue(result.isSuccess)
        val players = result.getOrNull()?.players.orEmpty()
        Assert.assertEquals(42, players[0].playerNumberOverlay?.number)
        Assert.assertEquals(null, players[1].playerNumberOverlay?.number)
        Assert.assertEquals(69, players[2].playerNumberOverlay?.number)
    }
}
