/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import com.telen.easylineup.lineup.defense.DefenseTestTags
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.openFixtureLineup
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A lineup with two extra hitters: two substitutes bat without playing the field, which also
 * pushes the slot reserved for a flexed pitcher two places further.
 */
@RunWith(AndroidJUnit4::class)
class ExtraHittersTest : KoinComponent {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule(
        SeedDatabaseRule.Mode.LINEUP_VARIANT,
        TeamType.BASEBALL,
        TeamStrategy.STANDARD,
        extraHitters = 2
    )

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    private val positions: PlayerFieldPositionRepository by inject()

    @Before
    fun openLineup() {
        composeTestRule.openFixtureLineup()
    }

    @Test
    fun offenseMarksSubstitutesInsideTheBattingOrderAsExtraHitters() {
        composeTestRule
            .onNodeWithText(string(R.string.new_lineup_tab_field_offense).uppercase())
            .performClick()
        composeTestRule.waitForText("Starter 1")

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText("Substitute 2"))
        composeTestRule.onNodeWithText("Substitute 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Substitute 2").assertIsDisplayed()
        composeTestRule
            .onAllNodesWithText(string(R.string.attack_extra_hitter_suffix), substring = true)
            .assertCountEquals(2)
    }

    @Test
    fun theFlexedPitcherBatsAfterTheExtraHitters() {
        composeTestRule.onNodeWithText(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))
        composeTestRule.onNodeWithTag(DefenseTestTags.DH_TOGGLE).performClick()
        composeTestRule.waitForText("Starter 10")
        composeTestRule.onNodeWithText("Starter 10").performClick()
        composeTestRule.waitForText(string(R.string.link_dh_and_pitcher_dialog_title))
        composeTestRule.onNodeWithText(string(R.string.action_confirm)).performClick()
        composeTestRule.waitForAbsence(string(R.string.link_dh_and_pitcher_dialog_title))
        composeTestRule.onNodeWithText(string(R.string.action_save_lineup)).performClick()
        composeTestRule.waitForAbsence(string(R.string.action_save_lineup))

        val pitcher = runBlocking {
            positions.getAllPlayerFieldPositionsForLineup(seedRule.fixture.lineupId)
        }.single { it.playerId == seedRule.fixture.playerIds[0] }
        assertEquals(12, pitcher.order)
    }
}
