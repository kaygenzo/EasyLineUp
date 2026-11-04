/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.ThemeTestUtils
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import com.telen.easylineup.utils.SharedPreferencesUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers [SettingsScreen]: render of the fixture's preference defaults, picking a lineup font
 * style, picking a day/night theme (a real [SharedPreferencesUtils]-backed, process-wide
 * setting - reset in [resetPreferencesAndTheme] so it can't leak into another test), opening
 * and cancelling the delete-all-data confirmation, back navigation, rotation, and light/dark
 * theme - the full matrix policy for a core screen (see the test plan).
 *
 * Not covered, deliberately: export/import (launch real `ActivityResultContract` file pickers,
 * the same external-flow boundary the import-flow's own Phase 3 coverage owns), the men/women
 * color swatches (the skydoves `ColorPickerDialog` is a non-Compose dialog, not driveable via
 * Compose test APIs - a pre-existing interop boundary per CLAUDE.md, not a regression),
 * licenses (`DialogFactory`'s plain `AlertDialog.Builder`, same interop boundary), the Play
 * Store row (launches a real external `Intent`), and actually confirming delete-all-data (wipes
 * the whole app and routes to `LoginActivity`, finishing `HomeActivity` - that is Welcome/
 * Login's own territory, same reasoning as TeamDetailsScreenTest skipping a real team delete).
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun openSettings() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText("SETTINGS").performClick()
        composeTestRule.waitForText("Settings")
    }

    @After
    fun resetPreferencesAndTheme() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SharedPreferencesUtils.putStringSetting(context, R.string.key_lineup_style, "0")
        SharedPreferencesUtils.putStringSetting(context, R.string.key_day_night_theme, "0")
        ThemeTestUtils.resetNightMode()
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun rendersSettingsFromFixture() {
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()
        composeTestRule.onNodeWithText("Lineup font").assertIsDisplayed()
        composeTestRule.onNodeWithText("Normal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("System").assertIsDisplayed()
    }

    @Test
    fun selectingLineupStyleUpdatesSubtitle() {
        composeTestRule.onNodeWithText("Lineup font").performClick()
        composeTestRule.waitForText("Hand writing")

        composeTestRule.onNodeWithText("Hand writing").performClick()
        composeTestRule.waitForAbsence("Normal")
        composeTestRule.onNodeWithText("Hand writing").assertIsDisplayed()
    }

    @Test
    fun selectingThemeUpdatesSubtitle() {
        composeTestRule.onNodeWithText("Theme").performClick()
        composeTestRule.waitForText("Night")

        composeTestRule.onNodeWithText("Night").performClick()
        composeTestRule.waitForAbsence("System")
        composeTestRule.onNodeWithText("Night").assertIsDisplayed()
    }

    @Test
    fun exportDataOpensConfirmSheetWithCountsAndCancelDismissesIt() {
        // loadExportPreview() is pure domain (just counts the current data) - the real system
        // folder picker only launches after tapping "Choose folder", which this test never
        // does, so this is reachable without touching that external-flow boundary.
        composeTestRule.onNodeWithText("Export data to file").performScrollTo().performClick()

        composeTestRule.waitForText("Export all data")
        // InfoCell always renders its label uppercased.
        composeTestRule.onNodeWithText("PLAYERS").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose folder").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForAbsence("Export all data")
        composeTestRule.onNodeWithText("Export data to file").assertIsDisplayed()
    }

    @Test
    fun deleteAllDataOpensConfirmationAndCancelDismissesIt() {
        composeTestRule.onNodeWithText("Delete all data").performClick()
        composeTestRule.waitForText("Cancel")

        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForAbsence("This action cannot be undo, are you sure ?")
        composeTestRule.onNodeWithText("Delete all data").assertIsDisplayed()
    }

    @Test
    fun backIconNavigatesToDashboard() {
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForAbsence("Theme")
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()
    }

    @Test
    fun rotatingPortraitToLandscapeKeepsContent() {
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForIdle()

        composeTestRule.waitForText("Settings")
        composeTestRule.onNodeWithText("Theme").performScrollTo().assertIsDisplayed()

        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForIdle()
    }

    @Test
    fun rendersInDarkTheme() {
        ThemeTestUtils.applyNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun rendersInLightTheme() {
        ThemeTestUtils.applyNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }
}
