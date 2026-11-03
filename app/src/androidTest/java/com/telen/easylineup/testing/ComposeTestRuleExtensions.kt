/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText

/**
 * Polls for a node with [text] instead of asserting immediately - a screen's first render (a
 * ViewModel's Room/Flow-backed state collection) and any state change triggered by a click
 * (another coroutine launch) both land on a later frame than the triggering action returns, so
 * a plain `waitForIdle()` followed by an assertion can race it. Every core-screen test's first
 * interaction after launch/navigation should go through this instead of asserting cold.
 */
fun ComposeTestRule.waitForText(
    text: String,
    useUnmergedTree: Boolean = false,
    timeoutMillis: Long = 5_000
) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text, useUnmergedTree = useUnmergedTree)
            .fetchSemanticsNodes().isNotEmpty()
    }
}

/**
 * Polls for a node with [text] to disappear instead of asserting `assertDoesNotExist()`
 * immediately - Navigation Compose's default per-destination exit transition keeps the screen
 * being left around (just no longer displayed) for its own animation, so the previous screen's
 * content is still in the tree, not yet gone, right after a navigating click returns.
 */
fun ComposeTestRule.waitForAbsence(
    text: String,
    useUnmergedTree: Boolean = false,
    timeoutMillis: Long = 5_000
) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text, useUnmergedTree = useUnmergedTree)
            .fetchSemanticsNodes().isEmpty()
    }
}
