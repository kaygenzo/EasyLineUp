/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.player.PlayerFormTestTags
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
 * Covers [RosterScreen]: render, every interaction, rotation, and light/dark theme - the full
 * matrix policy for a core screen (see the test plan). Reached by navigating from Dashboard's
 * roster-size stat, same as a real user would, rather than deep-linking the route directly.
 */
@RunWith(AndroidJUnit4::class)
class RosterScreenTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun openRoster() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText("12").performClick()
        // TeamViewModel's own Room/Flow-backed state lands a frame or more after navigating -
        // wait for the roster list itself, not just the static header title, before any test
        // body starts interacting.
        composeTestRule.waitForText(string(R.string.title_team))
        composeTestRule.waitForText("Starter 1")
    }

    @After
    fun resetThemeAndOrientation() {
        ThemeTestUtils.resetNightMode()
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun rendersRosterFromFixture() {
        // The header's own eyebrow renders the team name uppercased (ScreenHeader), unlike
        // Dashboard's un-uppercased header - using the wrong case here would silently match a
        // stale, not-yet-disposed Dashboard node instead of this screen's own.
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME.uppercase()).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.roster_sort_by_name).uppercase())
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.roster_sort_by_number).uppercase())
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Starter 1").assertIsDisplayed()
        // Last row in the LazyColumn - not yet composed at all at the initial scroll position
        // (virtualization), so a plain performScrollTo() has nothing to scroll to yet;
        // performScrollToNode scrolls the list incrementally until it appears.
        composeTestRule.onNodeWithTag(RosterTestTags.PLAYER_LIST)
            .performScrollToNode(hasText("Substitute 2"))
        composeTestRule.onNodeWithText("Substitute 2", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun sortPillsSwitchWithoutLosingContent() {
        composeTestRule
            .onNodeWithText(string(R.string.roster_sort_by_number).uppercase())
            .performClick()
        composeTestRule.onNodeWithText("Starter 1").assertIsDisplayed()

        composeTestRule
            .onNodeWithText(string(R.string.roster_sort_by_name).uppercase())
            .performClick()
        composeTestRule.onNodeWithText("Starter 1").assertIsDisplayed()
    }

    @Test
    fun playerRowNavigatesToPlayerDetailsAndBack() {
        composeTestRule.onNodeWithText("Starter 1").performClick()
        // PlayerProfileScreen reuses the exact same "Roster" title text (both are R.string.
        // title_team) - its own sort pills are what's actually unique to RosterScreen.
        composeTestRule.waitForAbsence(string(R.string.roster_sort_by_name).uppercase())

        pressBack()

        composeTestRule.waitForText(string(R.string.roster_sort_by_name).uppercase())
        composeTestRule.onNodeWithText(string(R.string.title_team)).assertIsDisplayed()
    }

    @Test
    fun addPlayerFabNavigatesToPlayerEditAndBack() {
        composeTestRule
            .onNodeWithContentDescription(string(R.string.roster_add_player_content_description))
            .performClick()
        composeTestRule.waitForAbsence(string(R.string.title_team))

        // The form's BackHandler always shows a discard-changes confirmation first, even with
        // nothing typed yet - confirm it to actually leave the screen.
        pressBack()
        composeTestRule.waitForText(string(R.string.generic_discard))
        composeTestRule.onNodeWithText(string(R.string.generic_discard)).performClick()

        composeTestRule.waitForText(string(R.string.title_team))
        composeTestRule.onNodeWithText(string(R.string.title_team)).assertIsDisplayed()
    }

    @Test
    fun creatingNewPlayerAddsThemToTheRoster() {
        composeTestRule
            .onNodeWithContentDescription(string(R.string.roster_add_player_content_description))
            .performClick()
        composeTestRule.waitForText(string(R.string.player_form_title_create))

        composeTestRule.onNodeWithTag(PlayerFormTestTags.NAME_FIELD)
            .performTextInput("Brand New Player")
        composeTestRule.onNodeWithText(string(R.string.save_button)).performClick()

        // A successful save navigates back to Roster - re-finding the new name there (not just
        // asserting the form closed) proves the player was actually written to the database,
        // not just reflected in the form's own in-memory state.
        composeTestRule.waitForAbsence(string(R.string.player_form_title_create))
        composeTestRule.waitForText("Brand New Player")
        composeTestRule.onNodeWithText("Brand New Player").assertIsDisplayed()
    }

    @Test
    fun messageTeamSheetShowsCountsAndEachActionIsSafeToTap() {
        composeTestRule
            .onNodeWithContentDescription(string(R.string.roster_message_team_content_description))
            .performClick()

        composeTestRule
            .onNodeWithText(string(R.string.roster_message_phone_count, 0, 12))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.roster_message_email_count, 0, 12))
            .assertIsDisplayed()

        // Both counts are 0 in the fixture, so these are no-ops in RosterRoute (no intent is
        // launched) - safe to tap without leaving the app.
        composeTestRule.onNodeWithText(string(R.string.roster_message_text_label)).performClick()
        composeTestRule.onNodeWithText(string(R.string.title_team)).assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription(string(R.string.roster_message_team_content_description))
            .performClick()
        composeTestRule.onNodeWithText(string(R.string.roster_message_email_label)).performClick()
        composeTestRule.onNodeWithText(string(R.string.title_team)).assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription(string(R.string.roster_message_team_content_description))
            .performClick()
        composeTestRule.onNodeWithText(string(R.string.roster_message_share_label)).performClick()
        // The resulting Toast is a separate system window, outside Compose's own semantics
        // tree - not asserted here; the sheet dismissing (back to the Roster header) is what's
        // actually under test.
        composeTestRule.onNodeWithText(string(R.string.title_team)).assertIsDisplayed()
    }

    @Test
    fun rotatingPortraitToLandscapeKeepsContent() {
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForIdle()

        composeTestRule.waitForText(TestFixtures.TEAM_NAME.uppercase())
        composeTestRule.onNodeWithText(string(R.string.title_team)).assertIsDisplayed()

        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForIdle()
    }

    @Test
    fun rendersInDarkTheme() {
        ThemeTestUtils.applyNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Starter 1").assertIsDisplayed()
    }

    @Test
    fun rendersInLightTheme() {
        ThemeTestUtils.applyNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Starter 1").assertIsDisplayed()
    }
}
