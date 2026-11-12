/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.inject
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression test for the bug migration 18 to 19 had to repair: Baseball 5 has no strategy
 * picker, and creating a lineup used to save the baseball default instead of its only strategy.
 */
@RunWith(AndroidJUnit4::class)
class Baseball5LineupCreationTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule(
        SeedDatabaseRule.Mode.LINEUP_VARIANT,
        TeamType.BASEBALL_5,
        TeamStrategy.B5_DEFAULT
    )

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    private val lineups: LineupRepository by inject()

    @Test
    fun createdLineupGetsTheBaseball5Strategy() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText(string(R.string.title_lineups).uppercase()).performClick()
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
        composeTestRule
            .onNodeWithText(string(R.string.lineup_creation_tournament_placeholder))
            .performClick()
        composeTestRule.waitForText(string(R.string.lineup_creation_tournament_sheet_title))
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForAbsence(string(R.string.lineup_creation_tournament_sheet_title))
        composeTestRule.onNodeWithText(string(R.string.lineup_creation_save)).performClick()
        composeTestRule.waitForAbsence(string(R.string.lineup_creation_header_title))

        val created = runBlocking { lineups.getLineups() }.single { it.name == "Scrimmage" }
        assertEquals(TeamStrategy.B5_DEFAULT.id, created.strategy)
    }
}
