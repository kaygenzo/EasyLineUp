/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.ThemeTestUtils
import com.telen.easylineup.testing.waitForText
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers [DashboardScreen]: render, every interaction, rotation, and light/dark theme - the
 * full matrix policy for a core screen (see the test plan).
 */
@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun waitForInitialLoad() {
        // DashboardViewModel's first state comes from a Room/Flow collection, which lands a
        // frame or more after the Activity launches - every test needs this settled before its
        // own interactions, not just the ones that happen to assert team-dependent content.
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
    }

    @After
    fun resetThemeAndOrientation() {
        ThemeTestUtils.resetNightMode()
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun rendersTeamStatsFromFixture() {
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()
        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME).assertIsDisplayed()
        composeTestRule.onNodeWithText("12").assertIsDisplayed()
        composeTestRule.onNodeWithText("Baseball").assertIsDisplayed()
    }

    @Test
    fun rosterSizeStatNavigatesToRoster() {
        composeTestRule.onNodeWithText("12").performClick()
        composeTestRule.onNodeWithText("Roster").assertIsDisplayed()
    }

    @Test
    fun lastLineupCardNavigatesAwayAndBackRestoresDashboard() {
        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME).performClick()
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertDoesNotExist()

        pressBack()

        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()
    }

    @Test
    fun shirtNumberLookupFindsPlayerAndOpensHistory() {
        // Shirt #2, not #1: every starter is tied at exactly one lineup appearance in the
        // fixture, and SQLite's tie-break for "most used" deterministically surfaces the
        // first-inserted player (Starter 1) - searching that same number would make its own
        // result text collide with the already-on-screen "Most used" stat.
        composeTestRule.onNodeWithTag(DashboardTestTags.SHIRT_NUMBER_FIELD).performTextInput("2")
        composeTestRule.onNodeWithTag(DashboardTestTags.SHIRT_NUMBER_FIELD).performImeAction()

        composeTestRule.waitForText("Starter 2")
        composeTestRule.onNodeWithText("Starter 2").assertIsDisplayed()

        composeTestRule.onNodeWithText("WHO WORE THIS NUMBER?").performClick()

        composeTestRule.onNodeWithText("Number 02 — history").assertIsDisplayed()
    }

    @Test
    fun rotatingPortraitToLandscapeKeepsContent() {
        // Device rotation is a system-level, not per-test, setting - don't assume the emulator
        // already starts in its natural orientation.
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(DashboardTestTags.SHIRT_NUMBER_FIELD).performTextInput("2")
        composeTestRule.onNodeWithTag(DashboardTestTags.SHIRT_NUMBER_FIELD).performImeAction()
        composeTestRule.waitForText("Starter 2")

        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForIdle()

        // The screen is a plain verticalScroll Column (every child stays composed regardless of
        // visibility), but landscape's shorter viewport can scroll the lookup result below the
        // fold - scroll it into view before asserting it's actually displayed. The result text
        // lives inside the lookup row's clickable Modifier, which merges it away from the
        // default merged-tree search once scrolling is involved, so look it up in the unmerged
        // tree instead (see the `useUnmergedTree` hint on the real failure this worked around).
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.waitForText("Starter 2", useUnmergedTree = true)
        composeTestRule.onNodeWithText("Starter 2", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()

        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForIdle()
    }

    @Test
    fun rendersInDarkTheme() {
        ThemeTestUtils.applyNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()
        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME).assertIsDisplayed()
    }

    @Test
    fun rendersInLightTheme() {
        ThemeTestUtils.applyNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()
        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME).assertIsDisplayed()
    }
}
