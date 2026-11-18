/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo

/**
 * How long a test waits for the UI to reach the state it expects. 5 s was enough on a local
 * emulator but not always on CI's, which render on the CPU: the first sheet a test class opened
 * on the API 28 one took longer. A test that passes doesn't wait any longer for it.
 */
const val UI_TIMEOUT_MILLIS = 10_000L

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
    timeoutMillis: Long = UI_TIMEOUT_MILLIS
) {
    waitUntilOrDump(timeoutMillis, "text \"$text\"") {
        onAllNodesWithText(text, useUnmergedTree = useUnmergedTree)
            .fetchSemanticsNodes().isNotEmpty()
    }
}

/**
 * Polls for a node tagged [tag] to exist - a freshly-navigated screen's content (e.g. a
 * position marker computed from its own ViewModel state) can lag a frame or more behind the
 * surrounding chrome that a `waitForText` already confirmed is up.
 */
fun ComposeTestRule.waitForTag(tag: String, timeoutMillis: Long = UI_TIMEOUT_MILLIS) {
    waitUntilOrDump(timeoutMillis, "tag \"$tag\"") {
        onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
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
    timeoutMillis: Long = UI_TIMEOUT_MILLIS
) {
    waitUntilOrDump(timeoutMillis, "\"$text\" to disappear") {
        onAllNodesWithText(text, useUnmergedTree = useUnmergedTree)
            .fetchSemanticsNodes().isEmpty()
    }
}

/**
 * Scrolls the node into view, when it sits in a scrollable container, then clicks it. A node
 * below the fold is still in the tree - so `waitForText` finds it - but a plain `performClick`
 * then injects its touch off-screen, under the bottom bar, and nothing happens: on a 360x640 dp
 * phone that is the DH slot under the field, the last settings, the rows low in a sheet.
 * `performScrollTo` alone would fail on a node with no scrollable ancestor.
 */
fun SemanticsNodeInteraction.scrollToAndClick(): SemanticsNodeInteraction {
    if (hasScrollableAncestor()) {
        performScrollTo()
    }
    return performClick()
}

/** [scrollToAndClick]'s check: whether any ancestor can scroll. */
fun SemanticsNodeInteraction.hasScrollableAncestor(): Boolean {
    var parent = fetchSemanticsNode().parent
    while (parent != null) {
        if (SemanticsActions.ScrollBy in parent.config) {
            return true
        }
        parent = parent.parent
    }
    return false
}
