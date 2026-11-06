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
import com.telen.easylineup.R
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.string
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
        composeTestRule.onNodeWithText(string(R.string.login_create)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.login_import)).assertIsDisplayed()
    }

    @Test
    fun createTeamOpensTeamCreationScreen() {
        composeTestRule.onNodeWithText(string(R.string.login_create)).performClick()
        composeTestRule.waitForText(string(R.string.team_form_title_create))

        // Close without saving - the discard confirmation is unconditional (same pattern as
        // PlayerEditRoute's back-press), so it fires even on this untouched form.
        composeTestRule
            .onNodeWithContentDescription(string(R.string.team_form_close_content_description))
            .performClick()
        composeTestRule.waitForText(string(R.string.generic_discard))
        composeTestRule.onNodeWithText(string(R.string.generic_discard)).performClick()

        composeTestRule.waitForAbsence(string(R.string.team_form_title_create))
        composeTestRule.onNodeWithText(string(R.string.login_create)).assertIsDisplayed()
    }
}
