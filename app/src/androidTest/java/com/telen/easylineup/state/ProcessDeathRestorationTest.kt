/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.state

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.openFixtureLineup
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * What a coach finds after Android killed the app in the background and they come back to it.
 * A test can't kill its own process, so this reproduces what such a return relies on: the
 * Activity is recreated from its saved state while every ViewModel is gone. The database and
 * Koin's singletons survive here, unlike a real kill; both are rebuilt from disk anyway.
 *
 * Unsaved input in a form is not kept: every form's state lives in its ViewModel, without a
 * SavedStateHandle, so the form comes back empty.
 */
@RunWith(AndroidJUnit4::class)
class ProcessDeathRestorationTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule(SeedDatabaseRule.Mode.CORE_FIXTURE_WITH_SECOND_TEAM)

    @get:Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

    /** Only on the Roster list: its absence tells the profile on top of it is showing. */
    private val sortPill get() = string(R.string.roster_sort_by_name).uppercase()

    private fun ActivityScenario<HomeActivity>.simulateProcessDeath() {
        onActivity { it.viewModelStore.clear() }
        recreate()
    }

    private fun launchHome() = ActivityScenario.launch(HomeActivity::class.java).also {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
    }

    @Test
    fun theOpenLineupComesBackWithItsBackStack() {
        launchHome().use { scenario ->
            composeTestRule.openFixtureLineup()

            scenario.simulateProcessDeath()

            composeTestRule.waitForText(TestFixtures.LINEUP_NAME.uppercase())
            composeTestRule.waitForText(string(R.string.new_lineup_tab_field_defense))
            pressBack()
            composeTestRule.waitForText(string(R.string.tournaments_list_header_title))
        }
    }

    @Test
    fun theOpenPlayerProfileComesBack() {
        launchHome().use { scenario ->
            composeTestRule.onNodeWithText(string(R.string.title_team).uppercase()).performClick()
            composeTestRule.waitForText("Starter 3")
            composeTestRule.onNodeWithText("Starter 3").performClick()
            composeTestRule.waitForAbsence(sortPill)

            scenario.simulateProcessDeath()

            composeTestRule.waitForText("Starter 3")
            composeTestRule.onNodeWithText(sortPill).assertDoesNotExist()
        }
    }

    @Test
    fun theOpenTeamSwitcherComesBack() {
        launchHome().use { scenario ->
            composeTestRule.onNodeWithText(TestFixtures.TEAM_NAME).performClick()
            composeTestRule.waitForText(TestFixtures.SECOND_TEAM_NAME)

            scenario.simulateProcessDeath()

            composeTestRule.waitForText(TestFixtures.SECOND_TEAM_NAME)
        }
    }

    @Test
    fun theLineupCreationFormComesBack() {
        launchHome().use { scenario ->
            composeTestRule.onNodeWithText(string(R.string.title_lineups).uppercase())
                .performClick()
            composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)
            composeTestRule
                .onNodeWithContentDescription(
                    string(R.string.tournaments_list_create_lineup_content_description)
                )
                .performClick()
            composeTestRule.waitForText(string(R.string.lineup_creation_header_title))
            composeTestRule
                .onNodeWithText(string(R.string.lineup_creation_name_placeholder))
                .performTextInput("Scrimmage")

            scenario.simulateProcessDeath()

            composeTestRule.waitForText(string(R.string.lineup_creation_header_title))
        }
    }
}
