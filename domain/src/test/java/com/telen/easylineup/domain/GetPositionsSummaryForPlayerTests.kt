/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PositionWithLineup
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.GetPositionsSummaryForPlayer
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetPositionsSummaryForPlayerTests {
    @Mock
    lateinit var playerFieldPositionsDao: PlayerFieldPositionRepository
    lateinit var getPositionsSummaryForPlayer: GetPositionsSummaryForPlayer
    lateinit var positions: List<PositionWithLineup>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        getPositionsSummaryForPlayer =
            GetPositionsSummaryForPlayer(playerFieldPositionsDao, testDispatcherProvider())

        val position1 = PositionWithLineup(position = FieldPosition.CATCHER.id)
        val position2 = PositionWithLineup(position = FieldPosition.DP_DH.id)
        val position3 = PositionWithLineup(position = FieldPosition.PITCHER.id)
        val position4 = PositionWithLineup(position = FieldPosition.CATCHER.id)
        val position5 = PositionWithLineup(position = FieldPosition.CATCHER.id)
        val position6 = PositionWithLineup(position = FieldPosition.PITCHER.id)
        val position7 = PositionWithLineup(position = FieldPosition.CATCHER.id)
        val position8 = PositionWithLineup(position = FieldPosition.RIGHT_FIELD.id)
        val position9 = PositionWithLineup(position = FieldPosition.CATCHER.id)
        val position10 = PositionWithLineup(position = FieldPosition.SUBSTITUTE.id)

        positions = mutableListOf(
            position1,
            position2,
            position3,
            position4,
            position5,
            position6,
            position7,
            position8,
            position9,
            position10
        )

        Mockito.`when`(playerFieldPositionsDao.getAllPositionsForPlayer(1L))
            .thenReturn(Single.just(positions))
    }

    @Test
    fun shouldTriggerAnExceptionIfPlayerIdIsNull() = runTest {
        val result = getPositionsSummaryForPlayer(null)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun shouldReturnMapOfAllPositions() = runTest {
        val result = getPositionsSummaryForPlayer(1L)
        assertTrue(result.isSuccess)
        val positionsSummary = result.getOrNull().orEmpty()
        Assert.assertEquals(5, positionsSummary.count())

        Assert.assertEquals(1, positionsSummary[FieldPosition.SUBSTITUTE])
        Assert.assertEquals(1, positionsSummary[FieldPosition.DP_DH])
        Assert.assertEquals(5, positionsSummary[FieldPosition.CATCHER])
        Assert.assertEquals(2, positionsSummary[FieldPosition.PITCHER])
        Assert.assertEquals(1, positionsSummary[FieldPosition.RIGHT_FIELD])
    }
}
