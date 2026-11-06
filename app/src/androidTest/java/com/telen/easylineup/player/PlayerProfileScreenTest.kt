/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.espresso.Espresso.closeSoftKeyboard
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
 * Covers [PlayerProfileScreen] (view) and [PlayerFormScreen] (edit), reached together via the
 * roster's "Starter 1" row - render, every interaction across both screens (edit/save,
 * validation, discard confirmation, delete, the profile pager's swipe), rotation, and
 * light/dark theme, the full matrix policy for a core screen (see the test plan).
 *
 * Roster is sorted alphabetically by default (case-insensitive): "Starter 1" is immediately
 * followed by "Starter 10" (a string-prefix, not "Starter 2" - "Starter 10" < "Starter 2"
 * lexicographically), which is what the pager swipe test expects next.
 */
@RunWith(AndroidJUnit4::class)
class PlayerProfileScreenTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun openStarterOneProfile() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText("12").performClick()
        composeTestRule.waitForText(string(R.string.title_team))
        composeTestRule.waitForText("Starter 1")
        composeTestRule.onNodeWithText("Starter 1").performClick()
        // PlayerViewModel's own state (license/positions) lands a frame or more after
        // navigating - the license number is unique to this screen once loaded.
        composeTestRule.waitForText("1000")
    }

    @After
    fun resetThemeAndOrientation() {
        ThemeTestUtils.resetNightMode()
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun rendersPlayerProfileFromFixture() {
        composeTestRule.onNodeWithText("Starter 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("1000").assertIsDisplayed()
        // No email/phone in the fixture - both InfoCells render the same "-" empty marker.
        composeTestRule.onAllNodesWithText("-").assertCountEquals(2)
    }

    @Test
    fun swipingPagerAdvancesToNextPlayerAlphabetically() {
        composeTestRule.onRoot().performTouchInput { swipeLeft() }

        composeTestRule.waitForText("Starter 10")
        composeTestRule.onNodeWithText("Starter 10").assertIsDisplayed()
    }

    @Test
    fun editNameAndSaveUpdatesProfile() {
        composeTestRule.onNodeWithContentDescription(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.player_form_title_edit))

        composeTestRule.onNodeWithTag(PlayerFormTestTags.NAME_FIELD).performTextClearance()
        composeTestRule.onNodeWithTag(PlayerFormTestTags.NAME_FIELD)
            .performTextInput("Starter 1 Edited")
        composeTestRule.onNodeWithText(string(R.string.save_button)).performClick()

        composeTestRule.waitForText("Starter 1 Edited")
        composeTestRule.onNodeWithText("Starter 1 Edited").assertIsDisplayed()
    }

    @Test
    fun validationBlocksEmptyName() {
        composeTestRule.onNodeWithContentDescription(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.player_form_title_edit))

        composeTestRule.onNodeWithTag(PlayerFormTestTags.NAME_FIELD).performTextClearance()
        composeTestRule.onNodeWithText(string(R.string.save_button)).performClick()

        composeTestRule.waitForText(string(R.string.player_creation_error_name_empty))
        composeTestRule.onNodeWithText(string(R.string.player_form_title_edit)).assertIsDisplayed()
    }

    @Test
    fun discardDialogKeepEditingPreservesFormDiscardLeavesUnsaved() {
        composeTestRule.onNodeWithContentDescription(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.player_form_title_edit))
        composeTestRule.onNodeWithTag(PlayerFormTestTags.NAME_FIELD).performTextClearance()
        composeTestRule.onNodeWithTag(PlayerFormTestTags.NAME_FIELD)
            .performTextInput("Unsaved Name")

        // A back press while the field still holds IME focus just dismisses the keyboard
        // first (standard Android behavior) - close it explicitly so this back press is the
        // one the Compose BackHandler actually sees.
        closeSoftKeyboard()
        pressBack()
        composeTestRule.waitForText(string(R.string.action_keep_editing))
        composeTestRule.onNodeWithText(string(R.string.action_keep_editing)).performClick()

        // Dismissing the confirm sheet must not have discarded the in-progress edit.
        composeTestRule.waitForAbsence(string(R.string.action_keep_editing))
        composeTestRule.onNodeWithText("Unsaved Name").assertIsDisplayed()

        closeSoftKeyboard()
        pressBack()
        composeTestRule.waitForText(string(R.string.generic_discard))
        composeTestRule.onNodeWithText(string(R.string.generic_discard)).performClick()

        composeTestRule.waitForText("Starter 1")
        composeTestRule.onNodeWithText("Starter 1").assertIsDisplayed()
    }

    @Test
    fun deletePlayerRemovesFromRoster() {
        composeTestRule.onNodeWithContentDescription(string(R.string.action_delete)).performClick()
        composeTestRule.waitForText(string(R.string.dialog_delete_player_title))

        composeTestRule.onNodeWithText(string(R.string.action_delete)).performClick()

        composeTestRule.waitForText(string(R.string.roster_sort_by_name).uppercase())
        composeTestRule.waitForAbsence("Starter 1")
    }

    @Test
    fun rotatingPortraitToLandscapeKeepsContent() {
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForIdle()

        composeTestRule.waitForText("Starter 1")
        composeTestRule.onNodeWithText("1000").assertIsDisplayed()

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
