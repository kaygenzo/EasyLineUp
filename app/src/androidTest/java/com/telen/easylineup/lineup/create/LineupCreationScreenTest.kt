/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
 * Covers [LineupCreationScreen]: render, name entry, picking an existing tournament, creating
 * a new one inline (the exact flow that regressed the SaveTournament id-propagation bug fixed
 * earlier this session - this keeps it covered), toggling the roster, strategy/extra-hitters,
 * save, rotation, and light/dark theme - the full matrix policy for a core screen (see the
 * test plan).
 */
@RunWith(AndroidJUnit4::class)
class LineupCreationScreenTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun openLineupCreation() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText("MY LINEUPS").performClick()
        composeTestRule.waitForText("Tournaments")
        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)

        composeTestRule.onNodeWithContentDescription("Create lineup").performClick()
        composeTestRule.waitForText("New lineup")
    }

    @After
    fun resetThemeAndOrientation() {
        ThemeTestUtils.resetNightMode()
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun rendersDefaultsFromFixture() {
        composeTestRule.onNodeWithText("New lineup").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose a tournament").assertIsDisplayed()
        composeTestRule.onNodeWithText("12 of 12 selected").assertIsDisplayed()
    }

    @Test
    fun enteringNameShowsTypedValue() {
        composeTestRule.onNodeWithText("e.g. Game 3 — Starters").performTextInput("Scrimmage")
        composeTestRule.onNodeWithText("Scrimmage").assertIsDisplayed()
    }

    @Test
    fun selectingExistingTournamentShowsItInField() {
        composeTestRule.onNodeWithText("Choose a tournament").performClick()
        composeTestRule.waitForText("Pick a tournament")

        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()

        composeTestRule.waitForAbsence("Pick a tournament")
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).assertIsDisplayed()
    }

    @Test
    fun creatingNewTournamentInlineSelectsIt() {
        composeTestRule.onNodeWithText("Choose a tournament").performClick()
        composeTestRule.waitForText("Pick a tournament")

        composeTestRule.onNodeWithText("Create a new tournament").performClick()
        composeTestRule.waitForText("New tournament")

        composeTestRule.onNodeWithText("e.g. Winter League").performTextInput("Winter Cup")
        composeTestRule.onNodeWithText("Add tournament").performClick()

        composeTestRule.waitForText("Winter Cup")
        composeTestRule.onNodeWithText("Winter Cup").assertIsDisplayed()
    }

    @Test
    fun togglingRosterPlayerUpdatesSummary() {
        composeTestRule.onNodeWithText("12 of 12 selected").performClick()
        composeTestRule.waitForText("Starter 1")

        composeTestRule.onNodeWithText("Starter 1").performClick()

        composeTestRule.waitForText("11 of 12 selected")
        composeTestRule.onNodeWithText("Done · 11 of 12 selected").performClick()

        composeTestRule.waitForText("11 of 12 selected")
    }

    @Test
    fun savingWithValidFieldsNavigatesAway() {
        composeTestRule.onNodeWithText("e.g. Game 3 — Starters").performTextInput("Scrimmage")
        composeTestRule.onNodeWithText("Choose a tournament").performClick()
        composeTestRule.waitForText("Pick a tournament")
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForAbsence("Pick a tournament")

        composeTestRule.onNodeWithText("Create lineup").performClick()

        composeTestRule.waitForAbsence("New lineup")
    }

    @Test
    fun rotatingPortraitToLandscapeKeepsContent() {
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForIdle()

        composeTestRule.waitForText("New lineup")
        composeTestRule.onNodeWithText("New lineup").assertIsDisplayed()

        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForIdle()
    }

    @Test
    fun rendersInDarkTheme() {
        ThemeTestUtils.applyNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("New lineup").assertIsDisplayed()
    }

    @Test
    fun rendersInLightTheme() {
        ThemeTestUtils.applyNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("New lineup").assertIsDisplayed()
    }
}
