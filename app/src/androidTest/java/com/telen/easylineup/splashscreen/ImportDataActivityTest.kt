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
 * Covers the no-`Intent.data` guard clause, a real and reachable path: the Activity can be
 * launched without a file, e.g. from a stale task-switcher entry, and finishes immediately
 * without reading anything. Actual imports are in [ImportFlowTest].
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
