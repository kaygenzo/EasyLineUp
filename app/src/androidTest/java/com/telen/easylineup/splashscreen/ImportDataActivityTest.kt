/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.splashscreen

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.testing.SeedDatabaseRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke coverage for `ImportDataActivity` (the `.elu` file-association entry point launched by
 * another app's share/open-with action) - Phase 3 (secondary/peripheral) depth per the test
 * plan: smoke only.
 *
 * Covers only the no-`Intent.data` guard clause (a real, reachable path: the Activity can be
 * launched without a file, e.g. a stale task-switcher entry), which finishes immediately without
 * reading anything. Not covered: an actual file import, valid or invalid. A valid payload would
 * need reproducing the app's own internal, Gson-backed `.elu` wire format
 * (`com.telen.easylineup.export.json`, deliberately `internal` and not a public contract to
 * build fixtures against) rather than the plain domain model; an invalid one only reaches a
 * plain `AlertDialog.Builder` (`DialogFactory`), a non-Compose interop boundary per CLAUDE.md,
 * same as Settings' licenses dialog. Both are disproportionate effort for a smoke-only screen.
 */
@RunWith(AndroidJUnit4::class)
class ImportDataActivityTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ImportDataActivity>()

    @Test
    fun launchingWithoutUriFinishesImmediately() {
        composeTestRule.waitUntil(5_000) {
            composeTestRule.activityRule.scenario.state == Lifecycle.State.DESTROYED
        }
    }
}
