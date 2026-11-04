/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers actually switching teams through [com.telen.easylineup.team.swap.SwapTeamSheet] -
 * [HomeActivityNavigationTest.swapTeamSheetOpensAndDismisses] only opens/dismisses the sheet,
 * since its single-team fixture can never make a real switch meaningful. Kept as its own class
 * since it needs a different seed (two teams) than every other test in the suite.
 */
@RunWith(AndroidJUnit4::class)
class SwapTeamTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule(SeedDatabaseRule.Mode.CORE_FIXTURE_WITH_SECOND_TEAM)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Test
    fun selectingAnotherTeamSwitchesTheActiveTeam() {
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)

        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).performClick()
        composeTestRule.waitForText(TestFixtures.SECOND_TEAM_NAME)

        composeTestRule.onNodeWithText(TestFixtures.SECOND_TEAM_NAME).performClick()

        composeTestRule.waitForAbsence(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText(TestFixtures.SECOND_TEAM_NAME).assertIsDisplayed()
    }
}
