/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.list

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.ThemeTestUtils
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers [TournamentListScreen]: render, every interaction (search, expand/collapse, navigate
 * to a lineup, create-lineup FAB, delete), rotation, and light/dark theme - the full matrix
 * policy for a core screen (see the test plan). The nested edit-lineup/roster sheet reachable
 * from here shares its roster-toggling logic with LineupCreation's own test, so it isn't
 * re-exercised in depth here.
 */
@RunWith(AndroidJUnit4::class)
class TournamentListScreenTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun openTournaments() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText("MY LINEUPS").performClick()
        composeTestRule.waitForText("Tournaments")
        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)
    }

    @After
    fun resetThemeAndOrientation() {
        ThemeTestUtils.resetNightMode()
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun rendersTournamentFromFixture() {
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).assertIsDisplayed()
        composeTestRule.onNodeWithText("Search tournaments or lineups…").assertIsDisplayed()
    }

    @Test
    fun searchFiltersAndClearsResults() {
        composeTestRule.onNodeWithText("Search tournaments or lineups…")
            .performTextInput("Nonexistent")
        composeTestRule.waitForText("No matches")
        composeTestRule.waitForAbsence(TestFixtures.TOURNAMENT_NAME)
    }

    @Test
    fun expandingTicketShowsLineupRow() {
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForText(TestFixtures.LINEUP_NAME)
        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME).assertIsDisplayed()
    }

    @Test
    fun lineupRowNavigatesToLineupScreen() {
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForText(TestFixtures.LINEUP_NAME)

        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME).performClick()
        composeTestRule.waitForAbsence("Tournaments")

        pressBack()
        composeTestRule.waitForText("Tournaments")
    }

    @Test
    fun editLineupIconOpensEditLineupSheetWithPreFilledFieldsAndRosterStep() {
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForText(TestFixtures.LINEUP_NAME)

        composeTestRule.onNodeWithContentDescription("Edit").performClick()
        composeTestRule.waitForText("Edit lineup")

        // The roster-selection step is reached from here but shares its own toggle logic with
        // LineupCreationScreenTest - only confirming it actually opens, not re-testing toggling.
        composeTestRule.onNodeWithText("12 of 12 selected").performClick()
        composeTestRule.waitForText("Starter 1")

        // The sheet's own roster-step back arrow shares its content description with the
        // screen's header back icon underneath it - it is the sheet's, since it is added to
        // the composition after (and thus matched last).
        composeTestRule.onAllNodesWithContentDescription("Back").onLast().performClick()
        composeTestRule.waitForText("Edit lineup")

        // Nothing was actually changed, so Cancel closes directly without a discard prompt.
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForAbsence("Edit lineup")
    }

    @Test
    fun createLineupFabNavigatesAwayAndBack() {
        composeTestRule.onNodeWithContentDescription("Create lineup").performClick()
        composeTestRule.waitForAbsence("Tournaments")

        pressBack()
        composeTestRule.waitForText("Tournaments")
    }

    @Test
    fun deletingTheOnlyTournamentShowsEmptyState() {
        composeTestRule.onNodeWithContentDescription("Delete").performClick()
        composeTestRule.waitForText("Delete tournament ${TestFixtures.TOURNAMENT_NAME} ?")

        composeTestRule.onNodeWithText("Delete").performClick()

        composeTestRule.waitForText("No tournaments yet")
        composeTestRule.waitForAbsence(TestFixtures.TOURNAMENT_NAME)
    }

    @Test
    fun rotatingPortraitToLandscapeKeepsContent() {
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForIdle()

        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).assertIsDisplayed()

        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForIdle()
    }

    @Test
    fun rendersInDarkTheme() {
        ThemeTestUtils.applyNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).assertIsDisplayed()
    }

    @Test
    fun rendersInLightTheme() {
        ThemeTestUtils.applyNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).assertIsDisplayed()
    }
}
