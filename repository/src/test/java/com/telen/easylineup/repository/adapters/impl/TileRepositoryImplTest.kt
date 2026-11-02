/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.repository.dao.TilesDao
import com.telen.easylineup.repository.model.RoomTile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify

/**
 * Unit tests for [TileRepositoryImpl]: verifies it maps domain<->Room correctly and delegates to
 * [TilesDao] - [TilesDao] itself is mocked, so this never touches a real database.
 */
@RunWith(MockitoJUnitRunner::class)
internal class TileRepositoryImplTest {
    @Mock
    lateinit var tilesDao: TilesDao

    private lateinit var repository: TileRepositoryImpl

    @Before
    fun setup() {
        repository = TileRepositoryImpl(tilesDao)
    }

    @Test
    fun shouldGetTilesMappedToDomain() = runTest {
        val rooms = listOf(
            RoomTile(id = 1L, position = 0, type = 1),
            RoomTile(id = 2L, position = 1, type = 2)
        )
        Mockito.`when`(tilesDao.getTiles()).thenReturn(rooms)

        val result = repository.getTiles()

        assertEquals(listOf(1L, 2L), result.map { it.id })
        assertEquals(listOf(0, 1), result.map { it.position })
    }

    @Test
    fun shouldUpdateTilesMappedToRoom() = runTest {
        val tiles = listOf(
            DashboardTile(id = 1L, position = 0, type = 1),
            DashboardTile(id = 2L, position = 1, type = 2)
        )

        repository.updateTiles(tiles)

        verify(tilesDao).updateTiles(argThat { size == 2 && map { it.position } == listOf(0, 1) })
    }

    @Test
    fun shouldInsertTilesMappedToRoom() = runTest {
        val tiles = listOf(DashboardTile(id = 0, position = 0, type = 1))

        repository.insertTiles(tiles)

        verify(tilesDao).insertTiles(argThat { size == 1 && first().type == 1 })
    }
}
