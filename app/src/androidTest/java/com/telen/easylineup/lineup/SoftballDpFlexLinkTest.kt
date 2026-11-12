/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import com.telen.easylineup.lineup.defense.DefenseTestTags
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.inject
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

/**
 * Softball's DP/Flex link: unlike baseball, the Flex is any field player the coach picks, so the
 * sheet starts with an empty slot and refuses to confirm until both are filled.
 */
@RunWith(AndroidJUnit4::class)
class SoftballDpFlexLinkTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule(
        SeedDatabaseRule.Mode.LINEUP_VARIANT,
        TeamType.SOFTBALL,
        TeamStrategy.STANDARD
    )

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    private val positions: PlayerFieldPositionRepository by inject()

    @Before
    fun openDpFlexSheet() {
        composeTestRule.openFixtureLineup()
        composeTestRule.onNodeWithText(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))
        composeTestRule.onNodeWithTag(DefenseTestTags.DH_TOGGLE).performClick()
        composeTestRule.waitForText("Starter 10")
        composeTestRule.onNodeWithText("Starter 10").performClick()
        composeTestRule.waitForText(string(R.string.link_dp_and_flex_dialog_title))
    }

    @Test
    fun confirmingWithoutAFlexIsRefused() {
        composeTestRule.onNodeWithText(string(R.string.action_confirm)).performClick()

        composeTestRule.waitForText(string(R.string.error_need_assign_both_players_message))
    }

    @Test
    fun pickingAFlexSavesTheLink() {
        val flexId = seedRule.fixture.playerIds[3]
        composeTestRule.onAllNodesWithText(string(R.string.field_position_flex)).onLast()
            .performClick()
        composeTestRule.waitForText("Starter 4")
        composeTestRule.onAllNodesWithText("Starter 4").onLast().performClick()
        composeTestRule.waitForText(string(R.string.action_confirm))

        composeTestRule.onNodeWithText(string(R.string.action_confirm)).performClick()
        composeTestRule.waitForAbsence(string(R.string.link_dp_and_flex_dialog_title))
        composeTestRule.onNodeWithText(string(R.string.action_save_lineup)).performClick()
        composeTestRule.waitForAbsence(string(R.string.action_save_lineup))

        val saved = runBlocking {
            positions.getAllPlayerFieldPositionsForLineup(seedRule.fixture.lineupId)
        }.associateBy { it.playerId }
        assertEquals(PlayerFieldPosition.FLAG_FLEX, saved.getValue(flexId).flags)
        assertEquals(10, saved.getValue(flexId).order)
        assertEquals(
            FieldPosition.DP_DH.id,
            saved.getValue(seedRule.fixture.playerIds[9]).position
        )
    }
}
