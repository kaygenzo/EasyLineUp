/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import com.telen.easylineup.lineup.attack.AttackTestTags
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.openFixtureLineup
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForTag
import com.telen.easylineup.testing.waitForText
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Reordering batters: long press a row's handle, then drag. Driven with raw touch input on the
 * handle, since the gesture has no accessibility action to trigger it another way.
 */
@RunWith(AndroidJUnit4::class)
class BattingOrderDragTest : KoinComponent {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    private val positions: PlayerFieldPositionRepository by inject()

    private val firstBatterId get() = seedRule.fixture.playerIds[0]
    private val secondBatterId get() = seedRule.fixture.playerIds[1]

    @Before
    fun openEditableOffense() {
        composeTestRule.openFixtureLineup()
        composeTestRule.onNodeWithText(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))
        composeTestRule
            .onNodeWithText(string(R.string.new_lineup_tab_field_offense).uppercase())
            .performClick()
        composeTestRule.waitForTag(AttackTestTags.dragHandle(firstBatterId))
    }

    private fun top(name: String) =
        composeTestRule.onNodeWithText(name).fetchSemanticsNode().boundsInRoot.top

    /** Holds the handle past the long-press delay, then moves down by [rows] rows. */
    private fun dragFirstBatterDown(rows: Float) {
        val rowPitch = top("Starter 2") - top("Starter 1")
        composeTestRule.onNodeWithTag(AttackTestTags.dragHandle(firstBatterId))
            .performTouchInput {
                down(center)
                advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN)
                val steps = 20
                repeat(steps) { moveBy(Offset(0f, rowPitch * rows / steps)) }
                up()
            }
        composeTestRule.waitForIdle()
    }

    @Test
    fun draggingTheFirstBatterDownOneRowSwapsTheirOrder() {
        dragFirstBatterDown(rows = 1.2f)

        assertTrue(top("Starter 2") < top("Starter 1"))
    }

    @Test
    fun theNewOrderIsSavedWithTheLineup() {
        dragFirstBatterDown(rows = 1.2f)

        composeTestRule.onNodeWithText(string(R.string.action_save_lineup)).performClick()
        composeTestRule.waitForAbsence(string(R.string.action_save_lineup))

        val saved = runBlocking {
            positions.getAllPlayerFieldPositionsForLineup(seedRule.fixture.lineupId)
        }.associateBy { it.playerId }
        assertEquals(2, saved.getValue(firstBatterId).order)
        assertEquals(1, saved.getValue(secondBatterId).order)
    }

    @Test
    fun aShortDragDoesNotReorder() {
        dragFirstBatterDown(rows = 0.4f)

        assertTrue(top("Starter 1") < top("Starter 2"))
    }

    private companion object {
        const val LONG_PRESS_MARGIN = 100L
    }
}
