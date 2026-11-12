/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.testing.ActivityTestUtils
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.inject
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForText
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Confirming "Delete all data" for real - [SettingsScreenTest] only cancels it. Its own class
 * because it ends outside [HomeActivity], on [LoginActivity], in a fresh task.
 */
@RunWith(AndroidJUnit4::class)
class DeleteAllDataTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    private val teams: TeamRepository by inject()
    private val players: PlayerRepository by inject()
    private val tournaments: TournamentRepository by inject()
    private val lineups: LineupRepository by inject()

    @After
    fun closeLoginScreen() {
        ActivityTestUtils.finishAll()
    }

    @Test
    fun confirmingWipesEverythingAndReturnsToLogin() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText(string(R.string.title_settings).uppercase()).performClick()
        composeTestRule.waitForText(string(R.string.title_settings))

        composeTestRule
            .onNodeWithText(string(R.string.setting_delete_data_description))
            .performClick()
        composeTestRule.waitForText(string(R.string.dialog_delete_cannot_undo_message))
        composeTestRule.onNodeWithText(string(R.string.action_delete)).performClick()

        ActivityTestUtils.waitForResumed(LoginActivity::class.java)
        runBlocking {
            assertTrue(teams.getTeams().isEmpty())
            assertTrue(players.getPlayers().isEmpty())
            assertTrue(tournaments.getTournaments().isEmpty())
            assertTrue(lineups.getLineups().isEmpty())
        }
    }
}
