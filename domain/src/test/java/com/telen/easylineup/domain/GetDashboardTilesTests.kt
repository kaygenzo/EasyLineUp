/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TilesRepository
import com.telen.easylineup.domain.usecases.CreateDashboardTiles
import com.telen.easylineup.domain.usecases.GetDashboardTiles
import com.telen.easylineup.domain.usecases.GetTeam
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetDashboardTilesTests {
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var playerFieldPositionDao: PlayerFieldPositionRepository
    @Mock lateinit var tilesRepo: TilesRepository
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getDashboardTiles: GetDashboardTiles

    private val team = Team(id = 1L, name = "Panthers", main = true)

    @Before
    fun init() {
        runBlocking {
        MockitoAnnotations.initMocks(this)
        getDashboardTiles = GetDashboardTiles(
            playerDao,
            lineupDao,
            playerFieldPositionDao,
            tilesRepo,
            GetTeam(teamDao, testDispatcherProvider()),
            CreateDashboardTiles(tilesRepo, testDispatcherProvider()),
            testDispatcherProvider()
        )
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(listOf(team))
    }
    }

    @Test
    fun shouldReturnExistingTiles() = runTest {
        val tile = DashboardTile(id = 1L, position = 0, type = TileType.TEAM_SIZE.type)
        Mockito.`when`(tilesRepo.getTiles()).thenReturn(listOf(tile))
        Mockito.`when`(playerDao.getPlayersByTeamId(team.id))
            .thenReturn(listOf(Player(id = 1L, teamId = 1L, name = "Toto", shirtNumber = 1, licenseNumber = 1L)))

        val result = getDashboardTiles()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun shouldCreateDefaultTilesWhenTeamHasNone() = runTest {
        val defaultTile = DashboardTile(id = 1L, position = 1, type = TileType.TEAM_SIZE.type)
        Mockito.`when`(tilesRepo.getTiles())
            .thenReturn(emptyList(), listOf(defaultTile))
        Mockito.`when`(tilesRepo.createTiles(any())).thenReturn(Unit)
        Mockito.`when`(playerDao.getPlayersByTeamId(team.id))
            .thenReturn(listOf(Player(id = 1L, teamId = 1L, name = "Toto", shirtNumber = 1, licenseNumber = 1L)))

        val result = getDashboardTiles()

        assertTrue(result.isSuccess)
        Mockito.verify(tilesRepo).createTiles(any())
        assertEquals(1, result.getOrNull()?.size)
    }
}
