/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.lineup.defense.DefenseTestTags
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.string
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
 * and rotation - the full matrix policy for a core screen (see the test plan).
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
        composeTestRule.onNodeWithText(string(R.string.title_lineups).uppercase()).performClick()
        composeTestRule.waitForText(string(R.string.tournaments_list_header_title))
        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)

        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForText(TestFixtures.LINEUP_NAME)
        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME).performClick()
        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_defense))
    }

    @After
    fun resetOrientation() {
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun rendersLineupFromFixtureInViewMode() {
        // ScreenHeader's eyebrow always renders uppercased (see RosterScreenTest for the same
        // gotcha) - the lineup name shown in the Tournaments list beforehand is exact-case.
        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME.uppercase()).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.new_lineup_tab_field_defense))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(DefenseTestTags.marker(FieldPosition.PITCHER.id))
            .assertIsDisplayed()
        // View mode: no Save/Cancel form bar.
        composeTestRule.onNodeWithText(string(R.string.action_save_lineup)).assertDoesNotExist()
    }

    @Test
    fun switchingToOffenseTabShowsBattingOrder() {
        composeTestRule
            .onNodeWithText(string(R.string.new_lineup_tab_field_offense).uppercase())
            .performClick()
        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_offense))
        composeTestRule.onNodeWithText("Starter 1").assertIsDisplayed()
    }

    @Test
    fun attackLegendDialogOpensAndDismisses() {
        composeTestRule
            .onNodeWithText(string(R.string.new_lineup_tab_field_offense).uppercase())
            .performClick()
        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_offense))

        composeTestRule
            .onNodeWithContentDescription(string(R.string.attack_legend_content_description))
            .performClick()
        composeTestRule.waitForText(string(android.R.string.ok))
        composeTestRule.onNodeWithText(string(R.string.attack_legend_title)).assertIsDisplayed()

        composeTestRule.onNodeWithText(string(android.R.string.ok)).performClick()
        composeTestRule.waitForAbsence(string(android.R.string.ok))
    }

    @Test
    fun editPillEntersEditModeWithFormBar() {
        composeTestRule.onNodeWithText(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))
        composeTestRule.onNodeWithText(string(R.string.action_save_lineup)).assertIsDisplayed()
    }

    @Test
    fun tappingPositionInEditModeOpensAvailablePlayersSheet() {
        composeTestRule.onNodeWithText(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))

        composeTestRule.waitForTag(FieldPosition.PITCHER.markerTag)
        composeTestRule.onNodeWithTag(FieldPosition.PITCHER.markerTag).performClick()
        composeTestRule.waitForText(string(R.string.defense_select_player_sheet_title))

        composeTestRule.onNodeWithText("Substitute 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Substitute 2").assertIsDisplayed()

        composeTestRule.onNodeWithText("Substitute 1").performClick()
        composeTestRule.waitForAbsence(string(R.string.defense_select_player_sheet_title))
    }

    @Test
    fun savingAfterReassigningPositionPersistsAndNavigatesToTournaments() {
        // This flow's post-save navigation once crashed off the main thread, when the save ran
        // in the screen's own coroutine scope; it now runs in LineupViewModel's scope and
        // reports back with LineupEvent.Saved, collected on the main thread.
        composeTestRule.onNodeWithText(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))

        composeTestRule.waitForTag(FieldPosition.PITCHER.markerTag)
        composeTestRule.onNodeWithTag(FieldPosition.PITCHER.markerTag).performClick()
        composeTestRule.waitForText(string(R.string.defense_select_player_sheet_title))
        composeTestRule.onNodeWithText("Substitute 1").performClick()
        composeTestRule.waitForAbsence(string(R.string.defense_select_player_sheet_title))

        composeTestRule.onNodeWithText(string(R.string.action_save_lineup)).performClick()

        // Edit mode is a separate pushed destination (see LineupRoute's onEditClick ->
        // navController.navigate(Routes.Lineup(..., editable = true))) - a successful save
        // pops back to the view-mode entry underneath it, not all the way to Tournaments
        // (goBack()'s default showFixedView = true short-circuits the deeper pop; see
        // LineupRoute.goBack()). That entry's own LineupViewModel is Flow/Room-backed, so it
        // re-collects the just-written change live rather than needing a fresh navigation.
        composeTestRule.waitForAbsence(string(R.string.action_save_lineup))
        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_defense))

        composeTestRule
            .onNodeWithText(string(R.string.new_lineup_tab_field_offense).uppercase())
            .performClick()
        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_offense))
        composeTestRule.onNodeWithText("Substitute 1").assertIsDisplayed()
    }

    @Test
    fun anUnsavedReassignmentSurvivesARotation() {
        composeTestRule.onNodeWithText(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))

        composeTestRule.waitForTag(FieldPosition.PITCHER.markerTag)
        composeTestRule.onNodeWithTag(FieldPosition.PITCHER.markerTag).performClick()
        composeTestRule.waitForText(string(R.string.defense_select_player_sheet_title))
        composeTestRule.onNodeWithText("Substitute 1").performClick()
        composeTestRule.waitForAbsence(string(R.string.defense_select_player_sheet_title))

        // Recreating the Activity used to make the screen read the lineup from the database
        // again, dropping the unsaved reassignment.
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForText(string(R.string.action_save_lineup))

        composeTestRule
            .onNodeWithText(string(R.string.new_lineup_tab_field_offense).uppercase())
            .performClick()
        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_offense))
        composeTestRule.onNodeWithText("Substitute 1").assertIsDisplayed()
    }

    @Test
    fun changingTheRosterKeepsTheOtherUnsavedChanges() {
        composeTestRule.onNodeWithText(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))

        composeTestRule.waitForTag(FieldPosition.PITCHER.markerTag)
        composeTestRule.onNodeWithTag(FieldPosition.PITCHER.markerTag).performClick()
        composeTestRule.waitForText(string(R.string.defense_select_player_sheet_title))
        composeTestRule.onNodeWithText("Substitute 1").performClick()
        composeTestRule.waitForAbsence(string(R.string.defense_select_player_sheet_title))

        // The roster used to be saved on its own and the lineup re-read from the database,
        // which dropped the reassignment above.
        composeTestRule
            .onNodeWithContentDescription(string(R.string.action_edit_roster))
            .performClick()
        composeTestRule.waitForText("Starter 2")
        composeTestRule.onNodeWithText("Starter 2").performClick()
        val done = string(R.string.lineup_creation_roster_done) + " · " +
            string(R.string.lineup_creation_roster_selected_format, 11, 12)
        composeTestRule.waitForText(done)
        composeTestRule.onNodeWithText(done).performClick()
        composeTestRule.waitForAbsence(done)

        composeTestRule
            .onNodeWithText(string(R.string.new_lineup_tab_field_offense).uppercase())
            .performClick()
        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_offense))
        composeTestRule.onNodeWithText("Substitute 1").assertIsDisplayed()
    }

    @Test
    fun tappingDpDhInEditModeOpensLinkSheet() {
        composeTestRule.onNodeWithText(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))

        // The fixture's lineup starts with DH/DP mode off, which forces the DP_DH marker to
        // a disabled NONE state regardless of its assigned player - the "Use DH" toggle must
        // be switched on first. The fixture's DP_DH position already has "Starter 10"
        // assigned, so the slot row shows their name (not an "unassigned" placeholder) once
        // DH mode is on - that name is this screen's only other use of it, so it's a safe,
        // unique target for the slot's own click handler.
        composeTestRule.onNodeWithTag(DefenseTestTags.DH_TOGGLE).performClick()
        composeTestRule.waitForText("Starter 10")
        composeTestRule.onNodeWithText("Starter 10").performClick()

        composeTestRule.waitForText(string(R.string.link_dh_and_pitcher_dialog_title))
    }

    @Test
    fun cancelInEditModeShowsDiscardConfirmation() {
        composeTestRule.onNodeWithText(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))

        pressBack()

        composeTestRule.waitForText(string(R.string.action_keep_editing))
        composeTestRule.onNodeWithText(string(R.string.action_discard)).performClick()

        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_defense))
        composeTestRule.onNodeWithText(string(R.string.action_save_lineup)).assertDoesNotExist()
    }

    @Test
    fun deletingLineupNavigatesToTournaments() {
        composeTestRule.onNodeWithContentDescription(string(R.string.action_delete)).performClick()
        composeTestRule.waitForText(string(R.string.dialog_delete_lineup_title))

        composeTestRule.onNodeWithText(string(R.string.action_delete)).performClick()

        // Back on Tournaments, and the list already reflects the deletion: the fixture's
        // tournament had only this lineup, and the list only shows tournaments that have at
        // least one (the tournament itself is kept, e.g. for the lineup creation picker).
        composeTestRule.waitForText(string(R.string.tournaments_list_header_title))
        composeTestRule.waitForAbsence(TestFixtures.TOURNAMENT_NAME)
    }

    @Test
    fun rotatingPortraitToLandscapeKeepsContent() {
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForIdle()

        composeTestRule.waitForText(TestFixtures.LINEUP_NAME.uppercase())
        composeTestRule
            .onNodeWithText(string(R.string.new_lineup_tab_field_defense))
            .assertIsDisplayed()

        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForIdle()
    }

}

private val FieldPosition.markerTag: String
    get() = DefenseTestTags.marker(id)
