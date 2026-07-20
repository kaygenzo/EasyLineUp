/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.InsertPlayerNumberOverlays
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class InsertPlayerNumberOverlaysTests {
    @Mock lateinit var playerDao: PlayerRepository
    lateinit var insertPlayerNumberOverlays: InsertPlayerNumberOverlays

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        insertPlayerNumberOverlays = InsertPlayerNumberOverlays(playerDao, testDispatcherProvider())
    }

    @Test
    fun shouldDelegateToRepository() = runTest {
        val overlays = listOf(PlayerNumberOverlay(id = 1L, lineupId = 10L, playerId = 1L, number = 8))
        Mockito.`when`(playerDao.createPlayerNumberOverlays(overlays)).thenReturn(Unit)

        val result = insertPlayerNumberOverlays(overlays)

        assertTrue(result.isSuccess)
        Mockito.verify(playerDao).createPlayerNumberOverlays(overlays)
    }
}
