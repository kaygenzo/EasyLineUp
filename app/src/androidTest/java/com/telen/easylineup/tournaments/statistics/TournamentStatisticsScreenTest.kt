/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.statistics

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers [TournamentStatisticsScreen]: render of the fixture's playing-time ranking, opening
 * the per-player position-usage drill-down picker, back navigation and rotation - the full
 * matrix policy for a core screen (see the test plan).
 */
@RunWith(AndroidJUnit4::class)
class TournamentStatisticsScreenTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun openTournamentStatistics() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText(string(R.string.title_lineups).uppercase()).performClick()
        composeTestRule.waitForText(string(R.string.tournaments_list_header_title))
        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)

        // The stats icon has no text of its own (only a content description) and sits on the
        // ticket's always-visible header row, not gated behind expanding it.
        composeTestRule
            .onNodeWithContentDescription(
                string(R.string.tournaments_list_stats_content_description)
            )
            .performClick()
        composeTestRule.waitForText(string(R.string.title_tournament_statistics))
    }

    @After
    fun resetOrientation() {
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun rendersPlayingTimeFromFixture() {
        // ScreenHeader's eyebrow always renders uppercased (see RosterScreenTest for the same
        // gotcha).
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME.uppercase())
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.title_tournament_statistics))
            .assertIsDisplayed()
        // The fixture's single lineup gives every starter exactly one game played - all tie,
        // so their row order isn't guaranteed, hence the scroll-to instead of a cold assert.
        // "Starter 1" also happens to be the position-usage field's default selection, so two
        // matches exist - the playing-time list's own row is always the first in tree order.
        composeTestRule.onAllNodesWithText("Starter 1").onFirst()
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun openingPlayerPickerAndSelectingUpdatesPositionUsage() {
        // The field's own text is whichever player the (tied) playing-time ranking happens to
        // put first - not predictable at test-time, so it's targeted by tag, not its text.
        composeTestRule.onNodeWithTag(TournamentStatisticsTestTags.PLAYER_SELECT_FIELD)
            .performClick()
        composeTestRule.waitForText("Starter 2")

        composeTestRule.onNodeWithText("Starter 2").performClick()
        composeTestRule.waitForAbsence(string(R.string.defense_select_player_sheet_title))
        composeTestRule.onNodeWithText("Starter 2").assertIsDisplayed()
    }

    @Test
    fun backIconNavigatesToTournaments() {
        composeTestRule.onNodeWithContentDescription(string(R.string.generic_back)).performClick()
        composeTestRule.waitForAbsence(string(R.string.title_tournament_statistics))
        composeTestRule.waitForText(string(R.string.tournaments_list_header_title))
    }

    @Test
    fun rotatingPortraitToLandscapeKeepsContent() {
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForIdle()

        composeTestRule.waitForText(string(R.string.title_tournament_statistics))
        composeTestRule.onAllNodesWithText("Starter 1").onFirst().assertIsDisplayed()

        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForIdle()
    }
}
