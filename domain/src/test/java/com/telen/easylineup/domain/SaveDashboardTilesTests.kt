package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.repository.TilesRepository
import com.telen.easylineup.domain.usecases.SaveDashboardTiles
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.core.Completable
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
    lateinit var mSaveDashboardTiles: SaveDashboardTiles
    val observer = TestObserver<SaveDashboardTiles.ResponseValue>()

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mSaveDashboardTiles = SaveDashboardTiles(tilesRepo)

        Mockito.`when`(tilesRepo.updateTiles(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerAnErrorIfSaveFails() {
        Mockito.`when`(tilesRepo.updateTiles(any())).thenReturn(Completable.error(IllegalStateException()))
        mSaveDashboardTiles.executeUseCase(SaveDashboardTiles.RequestValues(listOf())).subscribe(observer)
        observer.await()
        observer.assertError(IllegalStateException::class.java)
    }

    @Test
    fun shouldSaveTilesIfOrderAsc() {
        val list = mutableListOf<DashboardTile>().apply {
            add(DashboardTile(1, 1, TileType.TEAM_SIZE.type, true))
            add(DashboardTile(2, 2, TileType.MOST_USED_PLAYER.type, true))
            add(DashboardTile(3, 3, TileType.LAST_LINEUP.type, true))
            add(DashboardTile(4, 4, TileType.LAST_PLAYER_NUMBER.type, true))
        }
        mSaveDashboardTiles.executeUseCase(SaveDashboardTiles.RequestValues(list)).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(tilesRepo).updateTiles(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it[0].position)
            Assert.assertEquals(1, it[1].position)
            Assert.assertEquals(2, it[2].position)
            Assert.assertEquals(3, it[3].position)
        })
    }

    @Test
    fun shouldSaveTilesIfOrderDesc() {
        val list = mutableListOf<DashboardTile>().apply {
            add(DashboardTile(4, 4, TileType.LAST_PLAYER_NUMBER.type, true))
            add(DashboardTile(3, 3, TileType.LAST_LINEUP.type, true))
            add(DashboardTile(2, 2, TileType.MOST_USED_PLAYER.type, true))
            add(DashboardTile(1, 1, TileType.TEAM_SIZE.type, true))
        }
        mSaveDashboardTiles.executeUseCase(SaveDashboardTiles.RequestValues(list)).subscribe(observer)
        observer.await()
        observer.assertComplete()

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