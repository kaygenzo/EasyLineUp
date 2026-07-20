/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.CheckHashData
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class CheckHashDataTests {
    private val extraHitters = 0
    @Mock
    lateinit var teamDao: TeamRepository
    @Mock
    lateinit var playerDao: PlayerRepository
    @Mock
    lateinit var tournamentDao: TournamentRepository
    @Mock
    lateinit var lineupDao: LineupRepository
    @Mock
    lateinit var playerPositionsDao: PlayerFieldPositionRepository
    private lateinit var checkHash: CheckHashData

    @Before
    fun init() {
        runBlocking {
            MockitoAnnotations.initMocks(this)
            checkHash = CheckHashData(
                teamDao, playerDao, tournamentDao, lineupDao, playerPositionsDao,
                testDispatcherProvider()
            )

            val teams = mutableListOf(
                Team(1L, "A", null, 0, true, null),
                Team(2L, "B", null, 0, false, null)
            )

            val players = mutableListOf(
                Player(1L, 1L, "A", 1, 1L, null, 1, 0, 0, null),
                Player(2L, 2L, "B", 2, 2L, null, 1, 0, 0, null)
            )

            val tournaments = mutableListOf(
                Tournament(1L, "A", 1L, 2L, 3L, null, null),
                Tournament(2L, "B", 2L, 3L, 4L, null, null)
            )

            val lineups = mutableListOf(
                Lineup(
                    1L,
                    "A",
                    1L,
                    1L,
                    0,
                    TeamStrategy.STANDARD.id,
                    extraHitters,
                    3L,
                    1L,
                    2L,
                    null,
                    null
                ),
                Lineup(
                    2L,
                    "B",
                    2L,
                    2L,
                    0,
                    TeamStrategy.STANDARD.id,
                    extraHitters,
                    3L,
                    1L,
                    3L,
                    null,
                    null
                )
            )

            val playerPosition = mutableListOf(
                PlayerFieldPosition(1L, 1L, 1L, 0, 0f, 0f, 1, PlayerFieldPosition.FLAG_NONE, null),
                PlayerFieldPosition(2L, 2L, 2L, 0, 0f, 0f, 1, PlayerFieldPosition.FLAG_NONE, null)
            )

            Mockito.`when`(teamDao.getTeamsRx()).thenReturn(teams)
            Mockito.`when`(teamDao.updateTeamsWithRowCount(any())).thenReturn(2)

            Mockito.`when`(playerDao.getPlayers()).thenReturn(players)
            Mockito.`when`(playerDao.updatePlayersWithRowCount(any())).thenReturn(2)

            Mockito.`when`(tournamentDao.getTournaments()).thenReturn(tournaments)
            Mockito.`when`(tournamentDao.updateTournamentsWithRowCount(any())).thenReturn(2)

            Mockito.`when`(lineupDao.getLineups()).thenReturn(lineups)
            Mockito.`when`(lineupDao.updateLineupsWithRowCount(any())).thenReturn(2)

            Mockito.`when`(playerPositionsDao.getPlayerFieldPositions()).thenReturn(playerPosition)
            Mockito.`when`(playerPositionsDao.updatePlayerFieldPositionsWithRowCount(any()))
                .thenReturn(2)
        }
    }

    @Test
    fun shouldUpdateHashForAllEntries() = runTest {
        val result = checkHash()

        Assert.assertTrue(result.isSuccess)
        verify(teamDao).updateTeamsWithRowCount(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it.filter { it.hash == null }.size)
        })

        verify(playerDao).updatePlayersWithRowCount(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it.filter { it.hash == null }.size)
        })

        verify(tournamentDao).updateTournamentsWithRowCount(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it.filter { it.hash == null }.size)
        })

        verify(lineupDao).updateLineupsWithRowCount(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it.filter { it.hash == null }.size)
        })

        verify(playerPositionsDao).updatePlayerFieldPositionsWithRowCount(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it.filter { it.hash == null }.size)
        })

        Assert.assertArrayEquals(intArrayOf(2, 2, 2, 2, 2), result.getOrNull())
    }
}
