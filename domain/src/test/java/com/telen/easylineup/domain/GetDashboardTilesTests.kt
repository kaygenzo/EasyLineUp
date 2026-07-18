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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert.assertEquals
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
        MockitoAnnotations.initMocks(this)
        getDashboardTiles = GetDashboardTiles(
            playerDao,
            lineupDao,
            playerFieldPositionDao,
            tilesRepo,
            GetTeam(teamDao),
            CreateDashboardTiles(tilesRepo)
        )
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(listOf(team)))
    }

    @Test
    fun shouldReturnExistingTiles() {
        val tile = DashboardTile(id = 1L, position = 0, type = TileType.TEAM_SIZE.type)
        Mockito.`when`(tilesRepo.getTiles()).thenReturn(Single.just(listOf(tile)))
        Mockito.`when`(playerDao.getPlayersByTeamId(team.id))
            .thenReturn(Single.just(listOf(Player(id = 1L, teamId = 1L, name = "Toto", shirtNumber = 1, licenseNumber = 1L))))

        val observer = TestObserver<GetDashboardTiles.ResponseValue>()
        getDashboardTiles.executeUseCase(GetDashboardTiles.RequestValues()).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(1, observer.values().first().tiles.size)
    }

    @Test
    fun shouldCreateDefaultTilesWhenTeamHasNone() {
        val defaultTile = DashboardTile(id = 1L, position = 1, type = TileType.TEAM_SIZE.type)
        Mockito.`when`(tilesRepo.getTiles())
            .thenReturn(Single.just(emptyList()), Single.just(listOf(defaultTile)))
        Mockito.`when`(tilesRepo.createTiles(any())).thenReturn(Completable.complete())
        Mockito.`when`(playerDao.getPlayersByTeamId(team.id))
            .thenReturn(Single.just(listOf(Player(id = 1L, teamId = 1L, name = "Toto", shirtNumber = 1, licenseNumber = 1L))))

        val observer = TestObserver<GetDashboardTiles.ResponseValue>()
        getDashboardTiles.executeUseCase(GetDashboardTiles.RequestValues()).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(tilesRepo).createTiles(any())
        assertEquals(1, observer.values().first().tiles.size)
    }
}
