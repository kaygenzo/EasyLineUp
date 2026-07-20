/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.InsertPlayerFieldPositions
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
internal class InsertPlayerFieldPositionsTests {
    @Mock lateinit var playerFieldPositionDao: PlayerFieldPositionRepository
    lateinit var insertPlayerFieldPositions: InsertPlayerFieldPositions

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        insertPlayerFieldPositions =
            InsertPlayerFieldPositions(playerFieldPositionDao, testDispatcherProvider())
    }

    @Test
    fun shouldDelegateToRepository() = runTest {
        val positions = listOf(PlayerFieldPosition(id = 1L, playerId = 1L, lineupId = 10L))
        Mockito.`when`(playerFieldPositionDao.insertPlayerFieldPositions(positions))
            .thenReturn(Unit)

        val result = insertPlayerFieldPositions(positions)

        Assert.assertTrue(result.isSuccess)
        Mockito.verify(playerFieldPositionDao).insertPlayerFieldPositions(positions)
    }
}
