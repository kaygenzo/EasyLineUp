/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PositionWithLineup
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.GetPositionsSummaryForPlayer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetPositionsSummaryForPlayerTests {
    val observer: TestObserver<GetPositionsSummaryForPlayer.ResponseValue> = TestObserver()

    @Mock
    lateinit var playerFieldPositionsDao: PlayerFieldPositionRepository
    lateinit var getPositionsSummaryForPlayer: GetPositionsSummaryForPlayer
    lateinit var positions: List<PositionWithLineup>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        getPositionsSummaryForPlayer = GetPositionsSummaryForPlayer(playerFieldPositionsDao)

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
    fun shouldTriggerAnExceptionIfPlayerIdIsNull() {
        getPositionsSummaryForPlayer.executeUseCase(GetPositionsSummaryForPlayer.RequestValues(null))
            .subscribe(observer)
        observer.await()
        observer.assertError(IllegalArgumentException::class.java)
    }

    @Test
    fun shouldReturnMapOfAllPositions() {
        getPositionsSummaryForPlayer.executeUseCase(GetPositionsSummaryForPlayer.RequestValues(1L))
            .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(5, observer.values().first().summary.count())

        Assert.assertEquals(1, observer.values().first().summary[FieldPosition.SUBSTITUTE])
        Assert.assertEquals(1, observer.values().first().summary[FieldPosition.DP_DH])
        Assert.assertEquals(5, observer.values().first().summary[FieldPosition.CATCHER])
        Assert.assertEquals(2, observer.values().first().summary[FieldPosition.PITCHER])
        Assert.assertEquals(1, observer.values().first().summary[FieldPosition.RIGHT_FIELD])
    }
}
