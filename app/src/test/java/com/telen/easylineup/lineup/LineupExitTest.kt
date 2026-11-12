/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import org.junit.Assert.assertEquals
import org.junit.Test

class LineupExitTest {
    @Test
    fun aShortcutAlwaysReturnsToTheDashboard() {
        assertEquals(LineupExit.DASHBOARD, lineupExit(fromShortcut = true, afterDelete = false))
        assertEquals(LineupExit.DASHBOARD, lineupExit(fromShortcut = true, afterDelete = true))
    }

    @Test
    fun aDeletedLineupReturnsToTheTournaments() {
        assertEquals(LineupExit.TOURNAMENTS, lineupExit(fromShortcut = false, afterDelete = true))
    }

    @Test
    fun savingDiscardingOrGoingBackReturnsToThePreviousScreen() {
        assertEquals(LineupExit.PREVIOUS, lineupExit(fromShortcut = false, afterDelete = false))
    }
}
