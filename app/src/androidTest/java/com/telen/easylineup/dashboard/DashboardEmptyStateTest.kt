/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers [DashboardScreen]'s "no lineup yet" empty state (`HeroLastLineupCard` when
 * `lastLineup == null`) - a genuinely different branch (a plain message instead of a clickable
 * card) that [DashboardScreenTest]'s always-populated fixture never renders. Kept as its own
 * class rather than a test in that one, since it needs a different seed (team + roster, no
 * tournament/lineup at all).
 */
@RunWith(AndroidJUnit4::class)
class DashboardEmptyStateTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule(SeedDatabaseRule.Mode.TEAM_WITHOUT_LINEUP)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Test
    fun rendersNoLineupsYetMessageInsteadOfACard() {
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)

        composeTestRule
            .onNodeWithText(string(R.string.dashboard_hero_empty_title))
            .assertIsDisplayed()
    }
}
