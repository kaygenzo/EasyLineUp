/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.repository.TilesRepository
import com.telen.easylineup.domain.usecases.CreateDashboardTiles
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
internal class CreateDashboardTilesTests {
    @Mock
    lateinit var tilesRepo: TilesRepository
    lateinit var createDashboardTiles: CreateDashboardTiles

    @Before
    fun init() {
        runBlocking {
            MockitoAnnotations.initMocks(this)
            createDashboardTiles = CreateDashboardTiles(tilesRepo, testDispatcherProvider())
            Mockito.`when`(tilesRepo.createTiles(any())).thenReturn(Unit)
        }
    }

    @Test
    fun shouldCreateTheFourDefaultTilesAllActive() = runTest {
        val result = createDashboardTiles()

        assertTrue(result.isSuccess)
        val captor = argumentCaptor<List<DashboardTile>>()
        Mockito.verify(tilesRepo).createTiles(captor.capture())
        val tiles = captor.firstValue

        assertEquals(4, tiles.size)
        assertTrue(tiles.all { it.enabled })
        assertEquals(
            listOf(
                TileType.TEAM_SIZE.type,
                TileType.MOST_USED_PLAYER.type,
                TileType.LAST_LINEUP.type,
                TileType.LAST_PLAYER_NUMBER.type
            ),
            tiles.map { it.type }
        )
    }

    @Test
    fun shouldCreateTilesWithDistinctPositionsStartingAtOne() = runTest {
        val result = createDashboardTiles()

        assertTrue(result.isSuccess)
        val captor = argumentCaptor<List<DashboardTile>>()
        Mockito.verify(tilesRepo).createTiles(captor.capture())

        assertEquals(listOf(1, 2, 3, 4), captor.firstValue.map { it.position })
    }

    @Test
    fun shouldPropagateErrorFromRepository() = runTest {
        val exception = Exception("db error")
        Mockito.`when`(tilesRepo.createTiles(any())).thenAnswer { throw exception }

        val result = createDashboardTiles()

        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }
}
