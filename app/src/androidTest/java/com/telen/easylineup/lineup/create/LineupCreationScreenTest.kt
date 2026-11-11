/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import com.telen.easylineup.utils.DATE_SKELETON_WEEKDAY_DAY_MONTH
import com.telen.easylineup.utils.formatLocalizedDate
import java.util.Calendar
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Covers [LineupCreationScreen]: render, name entry, picking an existing tournament, creating
 * a new one inline (the exact flow that regressed the SaveTournament id-propagation bug fixed
 * earlier this session - this keeps it covered), toggling the roster, strategy/extra-hitters,
 * save, and rotation - the full matrix policy for a core screen (see the
 * test plan).
 */
@RunWith(AndroidJUnit4::class)
class LineupCreationScreenTest : KoinComponent {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    private val lineups: LineupRepository by inject()

    @Before
    fun openLineupCreation() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText(string(R.string.title_lineups).uppercase()).performClick()
        composeTestRule.waitForText(string(R.string.tournaments_list_header_title))
        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)

        composeTestRule
            .onNodeWithContentDescription(
                string(R.string.tournaments_list_create_lineup_content_description)
            )
            .performClick()
        composeTestRule.waitForText(string(R.string.lineup_creation_header_title))
    }

    @After
    fun resetOrientation() {
        OrientationTestUtils.rotateToPortrait()
    }

    @Test
    fun rendersDefaultsFromFixture() {
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_header_title))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_tournament_placeholder))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_roster_selected_format, 12, 12))
            .assertIsDisplayed()
    }

    @Test
    fun enteringNameShowsTypedValue() {
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_name_placeholder))
            .performTextInput("Scrimmage")
        composeTestRule.onNodeWithText("Scrimmage").assertIsDisplayed()
    }

    @Test
    fun selectingExistingTournamentShowsItInField() {
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_tournament_placeholder))
            .performClick()
        composeTestRule.waitForText(string(R.string.lineup_creation_tournament_sheet_title))

        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()

        composeTestRule.waitForAbsence(string(R.string.lineup_creation_tournament_sheet_title))
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).assertIsDisplayed()
    }

    @Test
    fun creatingNewTournamentInlineSelectsIt() {
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_tournament_placeholder))
            .performClick()
        composeTestRule.waitForText(string(R.string.lineup_creation_tournament_sheet_title))

        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_tournament_create_row))
            .performClick()
        composeTestRule.waitForText(string(R.string.lineup_creation_new_tournament_title))

        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_new_tournament_name_placeholder))
            .performTextInput("Winter Cup")
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_new_tournament_save))
            .performClick()

        composeTestRule.waitForText("Winter Cup")
        composeTestRule.onNodeWithText("Winter Cup").assertIsDisplayed()
    }

    @Test
    fun togglingRosterPlayerUpdatesSummary() {
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_roster_selected_format, 12, 12))
            .performClick()
        composeTestRule.waitForText("Starter 1")

        composeTestRule.onNodeWithText("Starter 1").performClick()

        composeTestRule.waitForText(string(R.string.lineup_creation_roster_selected_format, 11, 12))
        composeTestRule
            .onNodeWithText(
                string(R.string.lineup_creation_roster_done) + " · " +
                    string(R.string.lineup_creation_roster_selected_format, 11, 12)
            )
            .performClick()

        composeTestRule.waitForText(string(R.string.lineup_creation_roster_selected_format, 11, 12))
    }

    @Test
    fun savingWithValidFieldsNavigatesAway() {
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_name_placeholder))
            .performTextInput("Scrimmage")
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_tournament_placeholder))
            .performClick()
        composeTestRule.waitForText(string(R.string.lineup_creation_tournament_sheet_title))
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForAbsence(string(R.string.lineup_creation_tournament_sheet_title))

        composeTestRule.onNodeWithText(string(R.string.lineup_creation_save)).performClick()

        composeTestRule.waitForAbsence(string(R.string.lineup_creation_header_title))
    }

    @Test
    fun selectedExtraHittersAreSaved() {
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_name_placeholder))
            .performTextInput("Scrimmage")
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_tournament_placeholder))
            .performClick()
        composeTestRule.waitForText(string(R.string.lineup_creation_tournament_sheet_title))
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForAbsence(string(R.string.lineup_creation_tournament_sheet_title))
        // The extra-hitters row sits below the fold on small screens (and further once the
        // keyboard is up): scroll the form to it, as a user would, before tapping.
        composeTestRule.onNodeWithText("2").performScrollTo().performClick()

        composeTestRule.onNodeWithText(string(R.string.lineup_creation_save)).performClick()
        composeTestRule.waitForAbsence(string(R.string.lineup_creation_header_title))

        val created = runBlocking { lineups.getLineups() }.single { it.name == "Scrimmage" }
        assertEquals(2, created.extraHitters)
    }

    @Test
    fun savingWithEmptyNameShowsValidationError() {
        composeTestRule.onNodeWithText(string(R.string.lineup_creation_save)).performClick()

        composeTestRule.waitForText(string(R.string.lineup_creation_error_name_empty))
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_header_title))
            .assertIsDisplayed()
    }

    @Test
    fun savingWithNameButNoTournamentShowsValidationError() {
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_name_placeholder))
            .performTextInput("Scrimmage")

        composeTestRule.onNodeWithText(string(R.string.lineup_creation_save)).performClick()

        composeTestRule.waitForText(string(R.string.lineup_creation_error_tournament_empty))
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_header_title))
            .assertIsDisplayed()
    }

    @Test
    fun aDatePickedBeforeARotationIsStillAppliedAfterIt() {
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_tournament_placeholder))
            .performClick()
        composeTestRule.waitForText(string(R.string.lineup_creation_tournament_sheet_title))
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForAbsence(string(R.string.lineup_creation_tournament_sheet_title))

        // The fixture's tournament has no date window, so any day of this month is selectable.
        val today = Calendar.getInstance()
        val otherDay = Calendar.getInstance().apply {
            val day = today.get(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, if (day < 28) day + 1 else day - 1)
        }
        val todayLabel = formatLocalizedDate(today.timeInMillis, DATE_SKELETON_WEEKDAY_DAY_MONTH)
        composeTestRule.onNodeWithText(todayLabel).performClick()
        composeTestRule.waitForText(string(android.R.string.ok))
        // The picker is a dialog window of its own: its click actions are invoked directly
        // rather than through injected touches, which can land before that window is ready.
        // A day cell reads its full date ("Thursday, November 12, 2026"): match its localized
        // day, month and year so the test runs in any language.
        val otherDayCell = hasText(
            formatLocalizedDate(otherDay.timeInMillis, "yMMMMd"),
            substring = true,
            ignoreCase = true
        ) and hasClickAction()
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(otherDayCell).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(otherDayCell).performSemanticsAction(SemanticsActions.OnClick)

        // The old View-based picker came back after a rotation without its listener, so OK
        // closed it without applying anything.
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForText(string(android.R.string.ok))
        composeTestRule
            .onNodeWithText(string(android.R.string.ok))
            .performSemanticsAction(SemanticsActions.OnClick)

        composeTestRule.waitForAbsence(string(android.R.string.ok))
        composeTestRule.waitForText(
            formatLocalizedDate(otherDay.timeInMillis, DATE_SKELETON_WEEKDAY_DAY_MONTH)
        )
    }

    @Test
    fun rotatingPortraitToLandscapeKeepsContent() {
        OrientationTestUtils.rotateToLandscape()
        composeTestRule.waitForIdle()

        composeTestRule.waitForText(string(R.string.lineup_creation_header_title))
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_header_title))
            .assertIsDisplayed()

        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForIdle()
    }
}
