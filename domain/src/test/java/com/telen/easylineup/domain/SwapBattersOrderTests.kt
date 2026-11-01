/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.usecases.SwapBattersOrder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class SwapBattersOrderTests {
    private lateinit var swapBattersOrder: SwapBattersOrder

    private fun batter(id: Long, order: Int) = BatterState(
        playerId = id, playerFlag = 0, playerOrder = order, playerName = "p$id",
        playerNumber = id.toString(), playerPosition = FieldPosition.PITCHER,
        playerPositionDesc = "", canShowPosition = true, canMove = true,
        canShowDescription = true, canShowOrder = true, applyBackground = false,
        isEditable = true
    )

    @Before
    fun init() {
        swapBattersOrder = SwapBattersOrder()
    }

    @Test
    fun shouldExchangeTheTwoBattersOrders() {
        val from = batter(1L, order = 3)
        val to = batter(2L, order = 1)

        val (swappedFrom, swappedTo) = swapBattersOrder(from, to)

        assertEquals(1, swappedFrom.playerOrder)
        assertEquals(3, swappedTo.playerOrder)
    }

    @Test
    fun shouldKeepEachBattersIdentity() {
        val from = batter(1L, order = 3)
        val to = batter(2L, order = 1)

        val (swappedFrom, swappedTo) = swapBattersOrder(from, to)

        assertEquals(1L, swappedFrom.playerId)
        assertEquals(2L, swappedTo.playerId)
    }
}
