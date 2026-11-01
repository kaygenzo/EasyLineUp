/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.usecases.ToggleFavoritePosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class ToggleFavoritePositionTests {
    private lateinit var toggleFavoritePosition: ToggleFavoritePosition

    @Before
    fun init() {
        toggleFavoritePosition = ToggleFavoritePosition()
    }

    @Test
    fun shouldAddPositionWhenNotSelected() {
        val result = toggleFavoritePosition(emptySet(), FieldPosition.PITCHER)

        assertTrue(FieldPosition.PITCHER in result)
    }

    @Test
    fun shouldRemovePositionWhenAlreadySelected() {
        val result = toggleFavoritePosition(setOf(FieldPosition.PITCHER), FieldPosition.PITCHER)

        assertFalse(FieldPosition.PITCHER in result)
    }

    @Test
    fun shouldNotExceedMaxFavoritePositions() {
        val selected = setOf(FieldPosition.PITCHER, FieldPosition.CATCHER, FieldPosition.FIRST_BASE)

        val result = toggleFavoritePosition(selected, FieldPosition.SECOND_BASE)

        assertEquals(selected, result)
    }
}
