/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.repository.TilesRepository
import com.telen.easylineup.domain.usecases.SaveDashboardTiles
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
internal class SaveDashboardTilesTests {
    @Mock lateinit var tilesRepo: TilesRepository
    lateinit var saveDashboardTiles: SaveDashboardTiles

    @Before
    fun init() {
        runBlocking {
        MockitoAnnotations.initMocks(this)
        saveDashboardTiles = SaveDashboardTiles(tilesRepo, testDispatcherProvider())

        Mockito.`when`(tilesRepo.updateTiles(any())).thenReturn(Unit)
    }
    }

    @Test
    fun shouldTriggerAnErrorIfSaveFails() = runTest {
        Mockito.`when`(tilesRepo.updateTiles(any()))
            .thenAnswer { throw IllegalStateException() }
        val result = saveDashboardTiles(listOf())
        Assert.assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun shouldSaveTilesIfOrderAsc() = runTest {
        val list: MutableList<DashboardTile> = mutableListOf<DashboardTile>().apply {
            add(DashboardTile(1, 1, TileType.TEAM_SIZE.type, true))
            add(DashboardTile(2, 2, TileType.MOST_USED_PLAYER.type, true))
            add(DashboardTile(3, 3, TileType.LAST_LINEUP.type, true))
            add(DashboardTile(4, 4, TileType.LAST_PLAYER_NUMBER.type, true))
        }
        val result = saveDashboardTiles(list)
        Assert.assertTrue(result.isSuccess)

        verify(tilesRepo).updateTiles(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it[0].position)
            Assert.assertEquals(1, it[1].position)
            Assert.assertEquals(2, it[2].position)
            Assert.assertEquals(3, it[3].position)
        })
    }

    @Test
    fun shouldSaveTilesIfOrderDesc() = runTest {
        val list: MutableList<DashboardTile> = mutableListOf<DashboardTile>().apply {
            add(DashboardTile(4, 4, TileType.LAST_PLAYER_NUMBER.type, true))
            add(DashboardTile(3, 3, TileType.LAST_LINEUP.type, true))
            add(DashboardTile(2, 2, TileType.MOST_USED_PLAYER.type, true))
            add(DashboardTile(1, 1, TileType.TEAM_SIZE.type, true))
        }
        val result = saveDashboardTiles(list)
        Assert.assertTrue(result.isSuccess)

        verify(tilesRepo).updateTiles(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it[0].position)
            Assert.assertEquals(4, it[0].id)
            Assert.assertEquals(1, it[1].position)
            Assert.assertEquals(2, it[2].position)
            Assert.assertEquals(3, it[3].position)
            Assert.assertEquals(1, it[3].id)
        })
    }
}
