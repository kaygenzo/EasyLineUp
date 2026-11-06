/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.details

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.pluralString
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers [TeamDetailsScreen]: render of the fixture's team stats, opening and cancelling the
 * delete-team confirmation, and rotation - the full matrix policy for a core
 * screen (see the test plan).
 *
 * Not covered, deliberately: actually confirming the delete. The fixture's team is the only
 * team seeded, so a real delete leaves the app with none - a different, no-team-left flow
 * (routing back to Welcome/Login) that belongs with that screen's own (Phase 3, smoke-only)
 * coverage, not this one. Editing the team is also out of scope here: it launches
 * `TeamCreationActivity` via an `ActivityResultContract`, the same external-flow boundary
 * Welcome/team-creation's own Phase 3 coverage owns.
 */
@RunWith(AndroidJUnit4::class)
class TeamDetailsScreenTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun openTeamDetails() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText(string(R.string.title_settings).uppercase()).performClick()
        composeTestRule.waitForText(string(R.string.title_settings))

        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).performClick()
        // ScreenHeader's title isn't uppercased (unlike the "TEAM" section label on Settings
        // itself) - exact case disambiguates "we navigated" from "the Settings row is still
        // there".
        composeTestRule.waitForText(string(R.string.team_details_header_title))
    }

    @After
    fun resetOrientation() {
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun rendersTeamStatsFromFixture() {
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()
        // STANDARD strategy fixture: 10 starters + 2 substitutes = 12 players, no tournaments.
        composeTestRule
            .onNodeWithText(pluralString(R.plurals.team_details_roster_count, 12, 12))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.team_details_delete_title))
            .assertIsDisplayed()
    }

    @Test
    fun deleteRowOpensConfirmationAndCancelDismissesIt() {
        composeTestRule.onNodeWithText(string(R.string.team_details_delete_title)).performClick()
        composeTestRule.waitForText(string(R.string.cancel_button))
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()

        composeTestRule.onNodeWithText(string(R.string.cancel_button)).performClick()
        composeTestRule.waitForAbsence(string(R.string.dialog_delete_cannot_undo_message))
        // Still on TeamDetails, nothing deleted.
        composeTestRule
            .onNodeWithText(string(R.string.team_details_delete_title))
            .assertIsDisplayed()
    }

    @Test
    fun backIconNavigatesToSettings() {
        composeTestRule.onNodeWithContentDescription(string(R.string.generic_back)).performClick()
        composeTestRule.waitForAbsence(string(R.string.team_details_delete_title))
        composeTestRule.onNodeWithText(string(R.string.title_settings)).assertIsDisplayed()
    }

    @Test
    fun rotatingPortraitToLandscapeKeepsContent() {
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForIdle()

        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        // The root Column only scrolls vertically - in landscape "Delete team" can land below
        // the fold, not yet in the viewport.
        composeTestRule
            .onNodeWithText(string(R.string.team_details_delete_title))
            .performScrollTo()
            .assertIsDisplayed()

        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForIdle()
    }
}
