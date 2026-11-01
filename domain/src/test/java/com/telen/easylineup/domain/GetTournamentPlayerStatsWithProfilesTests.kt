/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerInLineup
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.StringResourcesProvider
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetPlayers
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTournamentPlayerStats
import com.telen.easylineup.domain.usecases.GetTournamentPlayerStatsWithProfiles
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
internal class GetTournamentPlayerStatsWithProfilesTests {
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var teamDao: TeamRepository
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var stringResourcesProvider: StringResourcesProvider
    lateinit var getTournamentPlayerStatsWithProfiles: GetTournamentPlayerStatsWithProfiles
    lateinit var team: Team
    lateinit var tournament: Tournament

    @Before
    fun init() {
        runBlocking {
            team = Team(id = 1L, name = "toto", type = 0, main = true)
            tournament = Tournament(
                id = 5L,
                name = "Summer cup",
                createdAt = 1000L,
                startTime = 2000L,
                endTime = 3000L
            )
            val getTeam = GetTeam(teamDao, testDispatcherProvider())
            getTournamentPlayerStatsWithProfiles = GetTournamentPlayerStatsWithProfiles(
                GetTournamentPlayerStats(
                    stringResourcesProvider,
                    lineupDao,
                    getTeam,
                    testDispatcherProvider()
                ),
                GetPlayers(playerDao, getTeam, testDispatcherProvider())
            )

            Mockito.`when`(teamDao.getTeams()).thenReturn(listOf(team))
            Mockito.`when`(lineupDao.getAllPlayerPositionsForTournament(tournament.id, team.id))
                .thenReturn(
                    listOf(
                        PlayerInLineup(
                            position = FieldPosition.SHORT_STOP.id,
                            playerName = "Jonas Alvarez",
                            playerId = 1L
                        )
                    )
                )
        }
    }

    @Test
    fun shouldMergeShirtNumberAndImageFromTheMatchingPlayer() = runTest {
        Mockito.`when`(playerDao.getPlayersByTeamId(team.id)).thenReturn(
            listOf(
                Player(
                    id = 1L,
                    teamId = team.id,
                    name = "Jonas Alvarez",
                    shirtNumber = 7,
                    licenseNumber = 0,
                    image = "content://photo"
                )
            )
        )

        val result = getTournamentPlayerStatsWithProfiles(tournament)

        assertTrue(result.isSuccess)
        val profile = result.getOrThrow().first { it.playerId == 1L }
        assertEquals(7, profile.shirtNumber)
        assertEquals("content://photo", profile.image)
    }

    @Test
    fun shouldDefaultToZeroAndNullWhenPlayerIsNoLongerOnTheRoster() = runTest {
        Mockito.`when`(playerDao.getPlayersByTeamId(team.id)).thenReturn(emptyList())

        val result = getTournamentPlayerStatsWithProfiles(tournament)

        assertTrue(result.isSuccess)
        val profile = result.getOrThrow().first { it.playerId == 1L }
        assertEquals(0, profile.shirtNumber)
        assertEquals(null, profile.image)
    }
}
