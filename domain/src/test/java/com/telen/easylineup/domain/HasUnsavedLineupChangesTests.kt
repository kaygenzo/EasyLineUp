/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.HasUnsavedLineupChanges
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class HasUnsavedLineupChangesTests {
    private val hasUnsavedLineupChanges = HasUnsavedLineupChanges()
    private val tournament = Tournament(name = "t", createdAt = 0, startTime = 0, endTime = 0)

    @Test
    fun anEmptyFormHasNothingToLose() {
        assertFalse(hasUnsavedLineupChanges(name = "", tournament = null))
    }

    @Test
    fun aBlankNameIsNotAChange() {
        assertFalse(hasUnsavedLineupChanges(name = "   ", tournament = null))
    }

    @Test
    fun aTypedNameIsAChange() {
        assertTrue(hasUnsavedLineupChanges(name = "Game 1", tournament = null))
    }

    @Test
    fun aPickedTournamentIsAChange() {
        assertTrue(hasUnsavedLineupChanges(name = "", tournament = tournament))
    }
}
