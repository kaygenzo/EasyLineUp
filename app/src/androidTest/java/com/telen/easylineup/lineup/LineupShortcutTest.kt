/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A lineup opened from the Dashboard's "last lineup" card is a shortcut: leaving it goes back to
 * the Dashboard, where a lineup opened from the Tournaments list goes back to that list.
 */
@RunWith(AndroidJUnit4::class)
class LineupShortcutTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    private val dashboardMarker get() = string(R.string.dashboard_lookup_hint).uppercase()

    @Before
    fun openTheLastLineupFromTheDashboard() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME).performClick()
        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_defense))
    }

    @Test
    fun deletingTheLineupReturnsToTheDashboard() {
        composeTestRule.onNodeWithContentDescription(string(R.string.action_delete)).performClick()
        composeTestRule.waitForText(string(R.string.dialog_delete_lineup_title))
        composeTestRule.onNodeWithText(string(R.string.action_delete)).performClick()

        composeTestRule.waitForText(dashboardMarker)
        composeTestRule.onNodeWithText(string(R.string.tournaments_list_header_title))
            .assertDoesNotExist()
    }

    @Test
    fun savingAnEditReturnsToTheLineupThenTheDashboard() {
        composeTestRule.onNodeWithText(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))
        composeTestRule.onNodeWithText(string(R.string.action_save_lineup)).performClick()
        composeTestRule.waitForAbsence(string(R.string.action_save_lineup))
        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_defense))

        pressBack()

        composeTestRule.waitForText(dashboardMarker)
    }
}
