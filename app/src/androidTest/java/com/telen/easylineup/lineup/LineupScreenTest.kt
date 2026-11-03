/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.lineup.defense.DefenseTestTags
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.ThemeTestUtils
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForTag
import com.telen.easylineup.testing.waitForText
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers [LineupScreen]: render, Defense/Offense tab switching, entering edit mode, assigning a
 * substitute to a position, the DP/Flex linking sheet, the discard confirmation, delete,
 * rotation, and light/dark theme - the full matrix policy for a core screen (see the test plan).
 *
 * Not covered, deliberately: the share action (tapping it launches a real system chooser Intent
 * - intercepting it without actually leaving the app would need the `espresso-intents`
 * dependency, out of scope for this pass, and leaving the app mid-test breaks window focus for
 * whatever test runs next) and Attack's drag-to-reorder (a fully custom raw-pointer drag with no
 * semantics reorder action, not reliably triggerable via plain touch-gesture simulation).
 */
@RunWith(AndroidJUnit4::class)
class LineupScreenTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun openLineup() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText("MY LINEUPS").performClick()
        composeTestRule.waitForText("Tournaments")
        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)

        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForText(TestFixtures.LINEUP_NAME)
        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME).performClick()
        composeTestRule.waitForText("Defense")
    }

    @After
    fun resetThemeAndOrientation() {
        ThemeTestUtils.resetNightMode()
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun rendersLineupFromFixtureInViewMode() {
        // ScreenHeader's eyebrow always renders uppercased (see RosterScreenTest for the same
        // gotcha) - the lineup name shown in the Tournaments list beforehand is exact-case.
        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME.uppercase()).assertIsDisplayed()
        composeTestRule.onNodeWithText("Defense").assertIsDisplayed()
        composeTestRule.onNodeWithTag(DefenseTestTags.marker(FieldPosition.PITCHER.id))
            .assertIsDisplayed()
        // View mode: no Save/Cancel form bar.
        composeTestRule.onNodeWithText("Save lineup").assertDoesNotExist()
    }

    @Test
    fun switchingToOffenseTabShowsBattingOrder() {
        composeTestRule.onNodeWithText("OFFENSE").performClick()
        composeTestRule.waitForText("Offense")
        composeTestRule.onNodeWithText("Starter 1").assertIsDisplayed()
    }

    @Test
    fun editPillEntersEditModeWithFormBar() {
        composeTestRule.onNodeWithText("Edit").performClick()
        composeTestRule.waitForText("Save lineup")
        composeTestRule.onNodeWithText("Save lineup").assertIsDisplayed()
    }

    @Test
    fun tappingPositionInEditModeOpensAvailablePlayersSheet() {
        composeTestRule.onNodeWithText("Edit").performClick()
        composeTestRule.waitForText("Save lineup")

        composeTestRule.waitForTag(FieldPosition.PITCHER.markerTag)
        composeTestRule.onNodeWithTag(FieldPosition.PITCHER.markerTag).performClick()
        composeTestRule.waitForText("Select a player")

        composeTestRule.onNodeWithText("Substitute 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Substitute 2").assertIsDisplayed()

        composeTestRule.onNodeWithText("Substitute 1").performClick()
        composeTestRule.waitForAbsence("Select a player")
    }

    @Test
    fun tappingDpDhInEditModeOpensLinkSheet() {
        composeTestRule.onNodeWithText("Edit").performClick()
        composeTestRule.waitForText("Save lineup")

        // The fixture's lineup starts with DH/DP mode off, which forces the DP_DH marker to
        // a disabled NONE state regardless of its assigned player - the "Use DH" toggle must
        // be switched on first. The fixture's DP_DH position already has "Starter 10"
        // assigned, so the slot row shows their name (not an "unassigned" placeholder) once
        // DH mode is on - that name is this screen's only other use of it, so it's a safe,
        // unique target for the slot's own click handler.
        composeTestRule.onNodeWithTag(DefenseTestTags.DH_TOGGLE).performClick()
        composeTestRule.waitForText("Starter 10")
        composeTestRule.onNodeWithText("Starter 10").performClick()

        composeTestRule.waitForText("Link your DH and your Pitcher")
    }

    @Test
    fun cancelInEditModeShowsDiscardConfirmation() {
        composeTestRule.onNodeWithText("Edit").performClick()
        composeTestRule.waitForText("Save lineup")

        pressBack()

        composeTestRule.waitForText("Keep editing")
        composeTestRule.onNodeWithText("Discard").performClick()

        composeTestRule.waitForText("Defense")
        composeTestRule.onNodeWithText("Save lineup").assertDoesNotExist()
    }

    @Test
    fun deletingLineupNavigatesToTournaments() {
        composeTestRule.onNodeWithContentDescription("Delete").performClick()
        composeTestRule.waitForText("Delete lineup ?")

        composeTestRule.onNodeWithText("Delete").performClick()

        // Deleting the lineup doesn't delete its tournament - back on Tournaments, still with
        // the fixture's one tournament, just none of its lineups left to expand.
        composeTestRule.waitForText("Tournaments")
        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)
    }

    @Test
    fun rotatingPortraitToLandscapeKeepsContent() {
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForIdle()

        composeTestRule.waitForText(TestFixtures.LINEUP_NAME.uppercase())
        composeTestRule.onNodeWithText("Defense").assertIsDisplayed()

        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForIdle()
    }

    @Test
    fun rendersInDarkTheme() {
        ThemeTestUtils.applyNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME.uppercase()).assertIsDisplayed()
    }

    @Test
    fun rendersInLightTheme() {
        ThemeTestUtils.applyNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME.uppercase()).assertIsDisplayed()
    }
}

private val FieldPosition.markerTag: String
    get() = DefenseTestTags.marker(id)
