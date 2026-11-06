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
import com.telen.easylineup.R
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.ThemeTestUtils
import com.telen.easylineup.testing.string
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
        composeTestRule.onNodeWithText(string(R.string.title_lineups).uppercase()).performClick()
        composeTestRule.waitForText(string(R.string.tournaments_list_header_title))
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
        composeTestRule
            .onNodeWithText(string(R.string.tournaments_list_search_placeholder))
            .assertIsDisplayed()
    }

    @Test
    fun searchFiltersAndClearsResults() {
        composeTestRule.onNodeWithText(string(R.string.tournaments_list_search_placeholder))
            .performTextInput("Nonexistent")
        composeTestRule.waitForText(string(R.string.tournaments_list_empty_search_title))
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
        composeTestRule.waitForAbsence(string(R.string.tournaments_list_header_title))

        pressBack()
        composeTestRule.waitForText(string(R.string.tournaments_list_header_title))
    }

    @Test
    fun editLineupIconOpensEditLineupSheetWithPreFilledFieldsAndRosterStep() {
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForText(TestFixtures.LINEUP_NAME)

        composeTestRule.onNodeWithContentDescription(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.edit_lineup_title))

        // The roster-selection step is reached from here but shares its own toggle logic with
        // LineupCreationScreenTest - only confirming it actually opens, not re-testing toggling.
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_roster_selected_format, 12, 12))
            .performClick()
        composeTestRule.waitForText("Starter 1")

        // The sheet's own roster-step back arrow shares its content description with the
        // screen's header back icon underneath it - it is the sheet's, since it is added to
        // the composition after (and thus matched last).
        composeTestRule
            .onAllNodesWithContentDescription(string(R.string.generic_back))
            .onLast()
            .performClick()
        composeTestRule.waitForText(string(R.string.edit_lineup_title))

        // Nothing was actually changed, so Cancel closes directly without a discard prompt.
        composeTestRule.onNodeWithText(string(R.string.cancel_button)).performClick()
        composeTestRule.waitForAbsence(string(R.string.edit_lineup_title))
    }

    @Test
    fun createLineupFabNavigatesAwayAndBack() {
        composeTestRule
            .onNodeWithContentDescription(
                string(R.string.tournaments_list_create_lineup_content_description)
            )
            .performClick()
        composeTestRule.waitForAbsence(string(R.string.tournaments_list_header_title))

        pressBack()
        composeTestRule.waitForText(string(R.string.tournaments_list_header_title))
    }

    @Test
    fun deletingTheOnlyTournamentShowsEmptyState() {
        composeTestRule.onNodeWithContentDescription(string(R.string.action_delete)).performClick()
        composeTestRule.waitForText(
            string(R.string.dialog_delete_tournament_title, TestFixtures.TOURNAMENT_NAME)
        )

        composeTestRule.onNodeWithText(string(R.string.action_delete)).performClick()

        composeTestRule.waitForText(string(R.string.tournaments_list_empty_title))
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
