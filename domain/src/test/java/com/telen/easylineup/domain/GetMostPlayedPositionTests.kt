/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.usecases.GetMostPlayedPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

internal class GetMostPlayedPositionTests {
    private lateinit var getMostPlayedPosition: GetMostPlayedPosition

    @Before
    fun init() {
        getMostPlayedPosition = GetMostPlayedPosition()
    }

    @Test
    fun shouldReturnThePositionWithTheHighestCount() {
        val result = getMostPlayedPosition(
            mapOf(FieldPosition.SHORT_STOP to 15, FieldPosition.PITCHER to 3)
        )

        assertEquals(FieldPosition.SHORT_STOP, result)
    }

    @Test
    fun shouldReturnNullWhenThereIsNoData() {
        assertNull(getMostPlayedPosition(emptyMap()))
    }

    @Test
    fun shouldReturnNullWhenTheTopCountIsZero() {
        assertNull(getMostPlayedPosition(mapOf(FieldPosition.SHORT_STOP to 0)))
    }
}
