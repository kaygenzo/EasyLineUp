/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerGamesCount
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.tiles.KEY_DATA_MATCH_PLAYED
import com.telen.easylineup.domain.model.tiles.KEY_DATA_NAME
import com.telen.easylineup.domain.model.tiles.KEY_DATA_SHIRT_NUMBER
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_EVENT_TIME
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_EXTRA_HITTERS
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_ID
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_NAME
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_STRATEGY
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_TOURNAMENT_NAME
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.ports.repository.TilesRepository
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.CreateDashboardTiles
import com.telen.easylineup.domain.usecases.GetDashboardTiles
import com.telen.easylineup.domain.usecases.GetTeam
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any

@RunWith(MockitoJUnitRunner::class)
internal class GetDashboardTilesTests {
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var playerFieldPositionDao: PlayerFieldPositionRepository
    @Mock lateinit var tilesRepo: TilesRepository
    @Mock lateinit var tournamentDao: TournamentRepository
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getDashboardTiles: GetDashboardTiles

    private val team = Team(id = 1L, name = "Panthers", main = true)

    @Before
    fun init() {
        runBlocking {
        getDashboardTiles = GetDashboardTiles(
            playerDao,
            lineupDao,
            playerFieldPositionDao,
            tilesRepo,
            tournamentDao,
            GetTeam(teamDao, testDispatcherProvider()),
            CreateDashboardTiles(tilesRepo, testDispatcherProvider()),
            testDispatcherProvider()
        )
        Mockito.`when`(teamDao.getTeams()).thenReturn(listOf(team))
    }
    }

    @Test
    fun shouldReturnExistingTiles() = runTest {
        val tile = DashboardTile(id = 1L, position = 0, type = TileType.TEAM_SIZE.type)
        Mockito.`when`(tilesRepo.getTiles()).thenReturn(listOf(tile))
        Mockito.`when`(playerDao.getPlayersByTeamId(team.id))
            .thenReturn(listOf(Player(
                id = 1L,
                teamId = 1L,
                name = "Toto",
                shirtNumber = 1,
                licenseNumber = "1"
            )))

        val result = getDashboardTiles()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun shouldCreateDefaultTilesWhenTeamHasNone() = runTest {
        val defaultTile = DashboardTile(id = 1L, position = 1, type = TileType.TEAM_SIZE.type)
        Mockito.`when`(tilesRepo.getTiles())
            .thenReturn(emptyList(), listOf(defaultTile))
        Mockito.`when`(tilesRepo.insertTiles(any())).thenReturn(Unit)
        Mockito.`when`(playerDao.getPlayersByTeamId(team.id))
            .thenReturn(listOf(Player(
                id = 1L,
                teamId = 1L,
                name = "Toto",
                shirtNumber = 1,
                licenseNumber = "1"
            )))

        val result = getDashboardTiles()

        assertTrue(result.isSuccess)
        Mockito.verify(tilesRepo).insertTiles(any())
        assertEquals(1, result.getOrNull()?.size)
    }

    private fun tournament(id: Long, name: String) =
        Tournament(id = id, name = name, createdAt = 0L, startTime = 0L, endTime = 0L)

    private fun tile(type: TileType) = DashboardTile(id = 1L, position = 0, type = type.type)

    private suspend fun singleTileData(type: TileType): Map<Int, Any>? {
        Mockito.`when`(tilesRepo.getTiles()).thenReturn(listOf(tile(type)))
        return getDashboardTiles().getOrThrow().singleOrNull()?.data?.getData()
    }

    @Test
    fun shouldResolveMostUsedPlayerTileFromTheFirstRankedPlayer() = runTest {
        Mockito.`when`(playerFieldPositionDao.getMostUsedPlayers(team.id)).thenReturn(
            listOf(PlayerGamesCount(playerId = 2L, size = 7), PlayerGamesCount(3L, 1))
        )
        Mockito.`when`(playerDao.getPlayerById(2L)).thenReturn(
            Player(id = 2L, teamId = 1L, name = "Ace", shirtNumber = 12, licenseNumber = "1")
        )

        val data = singleTileData(TileType.MOST_USED_PLAYER)

        assertEquals("Ace", data?.get(KEY_DATA_NAME))
        assertEquals(12, data?.get(KEY_DATA_SHIRT_NUMBER))
        assertEquals(7, data?.get(KEY_DATA_MATCH_PLAYED))
    }

    @Test
    fun shouldDropMostUsedPlayerTileWhenNobodyPlayedYet() = runTest {
        Mockito.`when`(playerFieldPositionDao.getMostUsedPlayers(team.id)).thenReturn(emptyList())

        assertNull(singleTileData(TileType.MOST_USED_PLAYER))
    }

    @Test
    fun shouldResolveLastLineupTileWithItsTournamentName() = runTest {
        Mockito.`when`(lineupDao.getLastLineup(team.id)).thenReturn(
            Lineup(
                id = 5L,
                name = "Game 1",
                teamId = 1L,
                tournamentId = 9L,
                strategy = TeamStrategy.SLOWPITCH.id,
                extraHitters = 2,
                eventTimeInMillis = 1_000L
            )
        )
        Mockito.`when`(tournamentDao.getTournaments()).thenReturn(
            listOf(tournament(8L, "Other"), tournament(9L, "Fall Classic"))
        )

        val data = singleTileData(TileType.LAST_LINEUP)

        assertEquals(5L, data?.get(KEY_LINEUP_ID))
        assertEquals("Game 1", data?.get(KEY_LINEUP_NAME))
        assertEquals(TeamStrategy.SLOWPITCH, data?.get(KEY_LINEUP_STRATEGY))
        assertEquals(2, data?.get(KEY_LINEUP_EXTRA_HITTERS))
        assertEquals(1_000L, data?.get(KEY_LINEUP_EVENT_TIME))
        assertEquals("Fall Classic", data?.get(KEY_LINEUP_TOURNAMENT_NAME))
    }

    @Test
    fun shouldNotLookUpATournamentForALineupWithoutOne() = runTest {
        Mockito.`when`(lineupDao.getLastLineup(team.id)).thenReturn(
            Lineup(id = 5L, name = "Game 1", teamId = 1L, tournamentId = 0L)
        )

        val data = singleTileData(TileType.LAST_LINEUP)

        assertFalse(data.orEmpty().containsKey(KEY_LINEUP_TOURNAMENT_NAME))
        Mockito.verify(tournamentDao, Mockito.never()).getTournaments()
    }

    @Test
    fun shouldOmitTheTournamentNameWhenItsTournamentNoLongerExists() = runTest {
        Mockito.`when`(lineupDao.getLastLineup(team.id)).thenReturn(
            Lineup(id = 5L, name = "Game 1", teamId = 1L, tournamentId = 9L)
        )
        Mockito.`when`(tournamentDao.getTournaments()).thenReturn(emptyList())

        val data = singleTileData(TileType.LAST_LINEUP)

        assertEquals("Game 1", data?.get(KEY_LINEUP_NAME))
        assertFalse(data.orEmpty().containsKey(KEY_LINEUP_TOURNAMENT_NAME))
    }

    @Test
    fun shouldDropLastLineupTileWhenTheTeamHasNoLineup() = runTest {
        Mockito.`when`(lineupDao.getLastLineup(team.id)).thenReturn(null)

        assertNull(singleTileData(TileType.LAST_LINEUP))
    }

    @Test
    fun shouldKeepLastPlayerNumberTileWithoutAnyLookup() = runTest {
        assertEquals(emptyMap<Int, Any>(), singleTileData(TileType.LAST_PLAYER_NUMBER))
    }

    @Test
    fun shouldDropATileOfAnUnknownType() = runTest {
        assertNull(singleTileData(TileType.BETA))
    }

    @Test
    fun shouldFailWhenThereIsNoTeam() = runTest {
        Mockito.`when`(teamDao.getTeams()).thenReturn(emptyList())

        assertTrue(getDashboardTiles().isFailure)
    }
}
