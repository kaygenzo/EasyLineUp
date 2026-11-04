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
import com.telen.easylineup.testing.SeedDatabaseRule
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
        composeTestRule.onNodeWithText("Create team").assertIsDisplayed()
        composeTestRule.onNodeWithText("BASEBALL").assertIsDisplayed()
        composeTestRule.onNodeWithText("SOFTBALL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
    }

    @Test
    fun enteringNameAndSelectingTypeThenSavingClosesForm() {
        composeTestRule.onNode(hasSetTextAction()).performTextInput("Riverside Hawks")
        composeTestRule.onNodeWithText("SOFTBALL").performClick()

        composeTestRule.onNodeWithText("Save").performClick()
        // A successful save calls finish() - no Compose hierarchy is left to query afterwards,
        // so the Activity's own lifecycle state is the only thing left to assert on.
        composeTestRule.waitUntil(5_000) {
            composeTestRule.activityRule.scenario.state == Lifecycle.State.DESTROYED
        }
    }

    @Test
    fun savingWithAnEmptyNameShowsValidationErrorAndStaysOnForm() {
        composeTestRule.onNodeWithText("Save").performClick()

        composeTestRule.waitForText("Team name cannot be empty")
        composeTestRule.onNodeWithText("Create team").assertIsDisplayed()
    }

    @Test
    fun closeIconShowsDiscardConfirmationAndKeepEditingDismissesIt() {
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        composeTestRule.waitForText("Keep editing")

        composeTestRule.onNodeWithText("Keep editing").performClick()
        composeTestRule.waitForAbsence("Keep editing")
        composeTestRule.onNodeWithText("Create team").assertIsDisplayed()
    }
}
