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
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.ports.repository.LineupRepository
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
 * Baseball's DH link: the pitcher stays on the mound without batting, so turning the DH on must
 * move them to the extra order slot and give their turn at bat to the DH. Checked in the database
 * after a real save, since the sheet only edits an in-memory copy until then.
 */
@RunWith(AndroidJUnit4::class)
class BaseballDhLinkTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    private val positions: PlayerFieldPositionRepository by inject()
    private val lineups: LineupRepository by inject()

    private val starter10Id get() = seedRule.fixture.playerIds[9]
    private val pitcherId get() = seedRule.fixture.playerIds[0]
    private val substituteId get() = seedRule.fixture.playerIds[10]

    @Before
    fun openDhSheet() {
        composeTestRule.openFixtureLineup()
        composeTestRule.onNodeWithText(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))
        composeTestRule.onNodeWithTag(DefenseTestTags.DH_TOGGLE).performClick()
        composeTestRule.waitForText("Starter 10")
        composeTestRule.onNodeWithText("Starter 10").performClick()
        composeTestRule.waitForText(string(R.string.link_dh_and_pitcher_dialog_title))
    }

    private fun confirmAndSave() {
        composeTestRule.onNodeWithText(string(R.string.action_confirm)).performClick()
        composeTestRule.waitForAbsence(string(R.string.link_dh_and_pitcher_dialog_title))
        composeTestRule.onNodeWithText(string(R.string.action_save_lineup)).performClick()
        composeTestRule.waitForAbsence(string(R.string.action_save_lineup))
    }

    private fun savedPositions() = runBlocking {
        positions.getAllPlayerFieldPositionsForLineup(seedRule.fixture.lineupId)
    }

    @Test
    fun confirmingKeepsTheDhAndMovesThePitcherOutOfTheBattingOrder() {
        confirmAndSave()

        val saved = savedPositions().associateBy { it.playerId }
        val lineup = runBlocking { lineups.getLineupById(seedRule.fixture.lineupId) }
        assertEquals(MODE_ENABLED, lineup.mode)
        assertEquals(FieldPosition.DP_DH.id, saved.getValue(starter10Id).position)
        assertEquals(PlayerFieldPosition.FLAG_FLEX, saved.getValue(pitcherId).flags)
        assertEquals(DESIGNATED_ORDER, saved.getValue(pitcherId).order)
    }

    @Test
    fun everyStarterKeepsADistinctBattingOrder() {
        confirmAndSave()

        val starters = savedPositions().filterNot { it.position == FieldPosition.SUBSTITUTE.id }
        assertEquals((1..DESIGNATED_ORDER).toList(), starters.map { it.order }.sorted())
    }

    @Test
    fun pickingAnotherDhReplacesThePreviousOne() {
        composeTestRule.onAllNodesWithText("Starter 10").onLast().performClick()
        composeTestRule.waitForText("Substitute 1")
        composeTestRule.onAllNodesWithText("Substitute 1").onLast().performClick()
        composeTestRule.waitForText(string(R.string.action_confirm))

        confirmAndSave()

        val saved = savedPositions().associateBy { it.playerId }
        assertEquals(FieldPosition.DP_DH.id, saved.getValue(substituteId).position)
        assertEquals(
            setOf(substituteId),
            saved.values.filter { it.position == FieldPosition.DP_DH.id }
                .map { it.playerId }
                .toSet()
        )
    }

    @Test
    fun cancellingLeavesTheDhAsItWas() {
        // The sheet's own button: edit mode's form bar has a Cancel of its own underneath.
        composeTestRule.onAllNodesWithText(string(android.R.string.cancel)).onLast().performClick()
        composeTestRule.waitForAbsence(string(R.string.link_dh_and_pitcher_dialog_title))

        val saved = savedPositions().associateBy { it.playerId }
        assertEquals(FieldPosition.DP_DH.id, saved.getValue(starter10Id).position)
        assertEquals(PlayerFieldPosition.FLAG_NONE, saved.getValue(pitcherId).flags)
    }

    private companion object {
        /** STANDARD's 9 batters, plus the slot the flexed pitcher moves to. */
        const val DESIGNATED_ORDER = 10
    }
}
