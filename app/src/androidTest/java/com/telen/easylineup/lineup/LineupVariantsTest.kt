/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.lineup.defense.DefenseTestTags
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForTag
import com.telen.easylineup.testing.waitForText
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Renders a lineup for every team type and strategy pair the app supports. [LineupScreenTest]
 * only uses baseball's standard one, while field layout, position count and batting order
 * size all depend on the strategy.
 */
@RunWith(Parameterized::class)
class LineupVariantsTest(private val teamType: TeamType, private val strategy: TeamStrategy) {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule(SeedDatabaseRule.Mode.LINEUP_VARIANT, teamType, strategy)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun openLineup() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText(string(R.string.title_lineups).uppercase()).performClick()
        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)
        composeTestRule.onNodeWithText(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForText(TestFixtures.LINEUP_NAME)
        composeTestRule.onNodeWithText(TestFixtures.LINEUP_NAME).performClick()
        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_defense))
    }

    @Test
    fun defenseShowsAMarkerForEveryFieldPositionOfTheStrategy() {
        strategy.positions.filterNot { it == FieldPosition.DP_DH }.forEach { position ->
            val tag = DefenseTestTags.marker(position.id)
            composeTestRule.waitForTag(tag)
            composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
        }
    }

    @Test
    fun offenseListsTheBatterOfEveryFieldPositionOfTheStrategy() {
        composeTestRule
            .onNodeWithText(string(R.string.new_lineup_tab_field_offense).uppercase())
            .performClick()
        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_offense))

        (1..strategy.batterSize).forEach { index ->
            composeTestRule.waitForText("Starter $index")
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}_{1}")
        fun variants(): List<Array<Any>> = listOf(
            arrayOf(TeamType.BASEBALL, TeamStrategy.FIVE_MAN_STANDARD),
            arrayOf(TeamType.SOFTBALL, TeamStrategy.STANDARD),
            arrayOf(TeamType.SOFTBALL, TeamStrategy.SLOWPITCH),
            arrayOf(TeamType.SOFTBALL, TeamStrategy.FIVE_MAN_STANDARD),
            arrayOf(TeamType.BASEBALL_5, TeamStrategy.B5_DEFAULT)
        )
    }
}
