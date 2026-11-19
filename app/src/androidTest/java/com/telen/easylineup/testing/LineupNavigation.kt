/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import com.telen.easylineup.R

/** From the dashboard to the fixture lineup's screen, for tests that start on that lineup. */
fun ComposeTestRule.openFixtureLineup() {
    OrientationTestUtils.rotateToPortrait()
    waitForText(TestFixtures.TEAM_NAME)
    onNodeWithText(string(R.string.title_lineups).uppercase()).scrollToAndClick()
    waitForText(TestFixtures.TOURNAMENT_NAME)
    onNodeWithText(TestFixtures.TOURNAMENT_NAME).scrollToAndClick()
    waitForText(TestFixtures.LINEUP_NAME)
    onNodeWithText(TestFixtures.LINEUP_NAME).scrollToAndClick()
    waitForText(string(R.string.new_lineup_tab_field_defense))
}

/**
 * Shows the lineup's offense: through its tab in portrait, while in landscape the lineup already
 * shows defense and offense side by side, with no tabbar.
 */
fun ComposeTestRule.showOffense() {
    val tab = onAllNodesWithText(string(R.string.new_lineup_tab_field_offense).uppercase())
    if (tab.fetchSemanticsNodes().isNotEmpty()) {
        tab.onFirst().scrollToAndClick()
    }
}
