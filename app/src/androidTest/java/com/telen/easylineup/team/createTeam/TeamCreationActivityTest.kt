/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.R
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke coverage for `TeamCreationActivity`/[TeamFormScreen] in its plain create-team mode
 * (reached from `WelcomeScreen`'s "Create your first team" or a later "add another team" flow)
 * - render, entering a name and saving, and the discard confirmation on close, per this
 * screen's Phase 3 (secondary/peripheral) depth policy in the test plan: no rotation/theme
 * matrix. The avatar picker isn't exercised here - it launches a real system photo picker
 * (`ActivityResultContracts.PickVisualMedia`), the same external-flow boundary as other
 * screens' image pickers.
 */
@RunWith(AndroidJUnit4::class)
class TeamCreationActivityTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<TeamCreationActivity>()

    @Test
    fun rendersCreateTeamForm() {
        composeTestRule.onNodeWithText(string(R.string.team_form_title_create)).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.title_baseball).uppercase())
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.title_softball).uppercase())
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.save_button)).assertIsDisplayed()
    }

    @Test
    fun enteringNameAndSelectingTypeThenSavingClosesForm() {
        composeTestRule.onNode(hasSetTextAction()).performTextInput("Riverside Hawks")
        composeTestRule.onNodeWithText(string(R.string.title_softball).uppercase()).performClick()

        composeTestRule.onNodeWithText(string(R.string.save_button)).performClick()
        // A successful save calls finish() - no Compose hierarchy is left to query afterwards,
        // so the Activity's own lifecycle state is the only thing left to assert on.
        composeTestRule.waitUntil(5_000) {
            composeTestRule.activityRule.scenario.state == Lifecycle.State.DESTROYED
        }
    }

    @Test
    fun savingWithAnEmptyNameShowsValidationErrorAndStaysOnForm() {
        composeTestRule.onNodeWithText(string(R.string.save_button)).performClick()

        composeTestRule.waitForText(string(R.string.team_creation_error_name_empty))
        composeTestRule.onNodeWithText(string(R.string.team_form_title_create)).assertIsDisplayed()
    }

    @Test
    fun closeIconShowsDiscardConfirmationAndKeepEditingDismissesIt() {
        composeTestRule
            .onNodeWithContentDescription(string(R.string.team_form_close_content_description))
            .performClick()
        composeTestRule.waitForText(string(R.string.action_keep_editing))

        composeTestRule.onNodeWithText(string(R.string.action_keep_editing)).performClick()
        composeTestRule.waitForAbsence(string(R.string.action_keep_editing))
        composeTestRule.onNodeWithText(string(R.string.team_form_title_create)).assertIsDisplayed()
    }
}
