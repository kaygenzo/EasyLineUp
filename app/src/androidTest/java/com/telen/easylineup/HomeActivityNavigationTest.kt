/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.string
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Replaces the old (deleted) `GlobalNavigationTest` - a thin smoke test for [HomeActivity]'s
 * own wiring (bottom nav + the globally-hosted swap-team sheet), not full per-screen coverage;
 * that lives in each screen's own test class (see the test plan).
 *
 * Screen identity is asserted with exact-case text matches: a destination's own header title
 * and the bottom nav's tab label often share the same string resource
 * (e.g. [R.string.title_team] = "Roster"), but the nav label is always rendered `.uppercase()`
 * while the header title isn't - so matching the exact case disambiguates "we navigated to the
 * Roster screen" from "the Roster nav item still shows" without needing a testTag here.
 */
@RunWith(AndroidJUnit4::class)
class HomeActivityNavigationTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun resetOrientation() {
        // Another test class's rotation test can leave the device rotated if it ran first in
        // this suite - orientation is a device-level, not per-test, setting.
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun bottomNavSwitchesBetweenTopLevelScreens() {
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()

        composeTestRule.onNodeWithText(string(R.string.title_team).uppercase()).performClick()
        composeTestRule.onNodeWithText(string(R.string.title_team)).assertIsDisplayed()

        composeTestRule.onNodeWithText(string(R.string.title_lineups).uppercase()).performClick()
        composeTestRule
            .onNodeWithText(string(R.string.tournaments_list_header_title))
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(string(R.string.title_settings).uppercase()).performClick()
        composeTestRule.onNodeWithText(string(R.string.title_settings)).assertIsDisplayed()

        composeTestRule
            .onNodeWithText(string(R.string.title_last_lineup).uppercase())
            .performClick()
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()
    }

    @Test
    fun swapTeamSheetOpensAndDismisses() {
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).performClick()

        // The Dashboard header behind the sheet still carries the team name too, so there are
        // two matching nodes once the sheet is open - the sheet's own title is enough to prove
        // it opened.
        composeTestRule.onNodeWithText(string(R.string.switch_team_sheet_title)).assertIsDisplayed()

        pressBack()

        composeTestRule
            .onNodeWithText(string(R.string.switch_team_sheet_title))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()
    }
}
