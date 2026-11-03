/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke coverage for [WelcomeScreen] (`LoginActivity`'s no-team entry point) - render and the
 * "Create your first team" action reaching `TeamCreationActivity`, per this screen's Phase 3
 * (secondary/peripheral) depth policy in the test plan: no rotation/theme matrix. Importing an
 * existing team isn't exercised here - it launches a real system file picker
 * (`ActivityResultContracts.OpenDocument`), the same external-flow boundary as Settings' own
 * import row.
 */
@RunWith(AndroidJUnit4::class)
class WelcomeScreenTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule(SeedDatabaseRule.Mode.EMPTY)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    @Test
    fun rendersWelcomeActions() {
        composeTestRule.onNodeWithText("Create your first team").assertIsDisplayed()
        composeTestRule.onNodeWithText("Import an existing team").assertIsDisplayed()
    }

    @Test
    fun createTeamOpensTeamCreationScreen() {
        composeTestRule.onNodeWithText("Create your first team").performClick()
        composeTestRule.waitForText("Create team")

        // Close without saving - the discard confirmation is unconditional (same pattern as
        // PlayerEditRoute's back-press), so it fires even on this untouched form.
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        composeTestRule.waitForText("Discard")
        composeTestRule.onNodeWithText("Discard").performClick()

        composeTestRule.waitForAbsence("Create team")
        composeTestRule.onNodeWithText("Create your first team").assertIsDisplayed()
    }
}
