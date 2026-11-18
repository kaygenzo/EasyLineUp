/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.printToString
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.io.ByteArrayOutputStream

private const val TAG = "FailureDiagnostics"
private const val LOG_CHUNK = 3_000
private val WINDOW_KEYS = listOf("mCurrentFocus", "mFocusedApp", "mInTouchMode")
private val IME_KEYS = listOf("mInputShown", "mShowRequested", "mCurFocusedWindow")

/**
 * [ComposeTestRule.waitUntil], logging what was on screen when it times out, then rethrowing.
 * GMD can't pull screenshots or other test output from a device below API 29, but it always
 * keeps the logcat. It has to be logged here, at the timeout, while the activity is still up: a
 * JUnit RunListener only hears of the failure once the rules have closed it.
 */
fun ComposeTestRule.waitUntilOrDump(
    timeoutMillis: Long,
    what: String,
    condition: () -> Boolean,
) {
    try {
        waitUntil(timeoutMillis, condition)
    } catch (timeout: ComposeTimeoutException) {
        dumpScreen("timed out waiting for $what")
        throw timeout
    }
}

/**
 * Logs, under the FailureDiagnostics tag: the focused window, touch mode, whether the soft
 * keyboard is showing, the semantics tree of every Compose root (the screen and each sheet or
 * dialog window), and the view hierarchy UiAutomator sees.
 */
fun ComposeTestRule.dumpScreen(reason: String) {
    log("--- $reason")
    runCatching {
        shell("dumpsys window").lines()
            .filter { line -> WINDOW_KEYS.any(line::contains) }
            .forEach { log("window: ${it.trim()}") }
        shell("dumpsys input_method").lines()
            .filter { line -> IME_KEYS.any(line::contains) }
            .forEach { log("ime: ${it.trim()}") }
    }.onFailure { log("dumpsys failed: $it") }
    runCatching {
        onAllNodes(isRoot(), useUnmergedTree = true).printToString(Int.MAX_VALUE)
            .lines().forEach { log("compose: $it") }
    }.onFailure { log("compose tree failed: $it") }
    runCatching {
        val hierarchy = ByteArrayOutputStream()
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            .dumpWindowHierarchy(hierarchy)
        hierarchy.toString().lines().forEach { log("ui: $it") }
    }.onFailure { log("ui hierarchy failed: $it") }
    log("--- end")
}

private fun shell(command: String): String {
    val output = InstrumentationRegistry.getInstrumentation().uiAutomation
        .executeShellCommand(command)
    return ParcelFileDescriptor.AutoCloseInputStream(output).bufferedReader().use { it.readText() }
}

private fun log(line: String) {
    // Logcat cuts a line at about 4 KB.
    line.chunked(LOG_CHUNK).forEach { Log.i(TAG, it) }
}
