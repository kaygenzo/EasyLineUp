/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.ThemeMode
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.ThemeTestUtils
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import com.telen.easylineup.utils.SharedPreferencesUserPreferencesStore
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers [SettingsScreen]: render of the fixture's preference defaults, picking a day/night
 * theme (a real [SharedPreferencesUserPreferencesStore]-backed, process-wide
 * setting - reset in [resetPreferencesAndTheme] so it can't leak into another test), opening
 * and cancelling the delete-all-data confirmation, back navigation and rotation - the full
 * matrix policy for a core screen (see the test plan).
 *
 * Not covered, deliberately: export/import (launch real `ActivityResultContract` file pickers,
 * the same external-flow boundary the import-flow's own Phase 3 coverage owns), the men/women
 * color swatches (the skydoves `ColorPickerDialog` is a non-Compose dialog, not driveable via
 * Compose test APIs - a pre-existing interop boundary per CLAUDE.md, not a regression),
 * licenses (`LicensesManager`'s plain native dialog, same interop boundary), the Play
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
        composeTestRule.onNodeWithText(string(R.string.title_settings).uppercase()).performClick()
        composeTestRule.waitForText(string(R.string.title_settings))
    }

    @After
    fun resetPreferencesAndTheme() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SharedPreferencesUserPreferencesStore(context).setThemeMode(ThemeMode.FOLLOW_SYSTEM)
        ThemeTestUtils.resetNightMode()
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun rendersSettingsFromFixture() {
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.setting_day_night_style_description))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.lineup_theme_system_value))
            .assertIsDisplayed()
    }

    @Test
    fun selectingThemeUpdatesSubtitle() {
        composeTestRule
            .onNodeWithText(string(R.string.setting_day_night_style_description))
            .performClick()
        composeTestRule.waitForText(string(R.string.lineup_theme_night_value))

        composeTestRule.onNodeWithText(string(R.string.lineup_theme_night_value)).performClick()
        composeTestRule.waitForAbsence(string(R.string.lineup_theme_system_value))
        composeTestRule
            .onNodeWithText(string(R.string.lineup_theme_night_value))
            .assertIsDisplayed()
    }

    @Test
    fun exportDataOpensConfirmSheetWithCountsAndCancelDismissesIt() {
        // loadExportPreview() is pure domain (just counts the current data) - the real system
        // folder picker only launches after tapping "Choose folder", which this test never
        // does, so this is reachable without touching that external-flow boundary.
        composeTestRule
            .onNodeWithText(string(R.string.setting_export_data_description))
            .performScrollTo()
            .performClick()

        composeTestRule.waitForText(string(R.string.export_confirm_title))
        // InfoCell always renders its label uppercased. In French that label is also the bottom
        // nav's Roster tab, the clickable match to exclude.
        composeTestRule
            .onNode(
                hasText(string(R.string.data_transfer_count_players).uppercase()) and
                    hasClickAction().not()
            )
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.export_confirm_action))
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(string(R.string.cancel_button)).performClick()
        composeTestRule.waitForAbsence(string(R.string.export_confirm_title))
        composeTestRule
            .onNodeWithText(string(R.string.setting_export_data_description))
            .assertIsDisplayed()
    }

    @Test
    fun deleteAllDataOpensConfirmationAndCancelDismissesIt() {
        composeTestRule
            .onNodeWithText(string(R.string.setting_delete_data_description))
            .performClick()
        composeTestRule.waitForText(string(R.string.cancel_button))

        composeTestRule.onNodeWithText(string(R.string.cancel_button)).performClick()
        composeTestRule.waitForAbsence(string(R.string.dialog_delete_cannot_undo_message))
        composeTestRule
            .onNodeWithText(string(R.string.setting_delete_data_description))
            .assertIsDisplayed()
    }

    @Test
    fun backIconNavigatesToDashboard() {
        composeTestRule.onNodeWithContentDescription(string(R.string.generic_back)).performClick()
        composeTestRule.waitForAbsence(string(R.string.setting_day_night_style_description))
        composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).assertIsDisplayed()
    }

    @Test
    fun rotatingPortraitToLandscapeKeepsContent() {
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForIdle()

        composeTestRule.waitForText(string(R.string.title_settings))
        composeTestRule
            .onNodeWithText(string(R.string.setting_day_night_style_description))
            .performScrollTo()
            .assertIsDisplayed()

        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForIdle()
    }
}
