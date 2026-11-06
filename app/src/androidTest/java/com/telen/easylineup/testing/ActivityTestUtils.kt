/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.junit.Assert.fail

/** For flows that hop between Activities, which a single-Activity ComposeTestRule can't follow. */
object ActivityTestUtils {
    private const val POLL_INTERVAL_MILLIS = 100L

    private fun resumedActivity(): Activity? {
        var activity: Activity? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            activity = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .firstOrNull()
        }
        return activity
    }

    fun waitForResumed(activityClass: Class<out Activity>, timeoutMillis: Long = 10_000) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            if (activityClass.isInstance(resumedActivity())) {
                return
            }
            Thread.sleep(POLL_INTERVAL_MILLIS)
        }
        fail("${activityClass.simpleName} not resumed, found ${resumedActivity()}")
    }

    fun launch(
        context: Context,
        activityClass: Class<out Activity>,
        intentSetup: Intent.() -> Unit = {}
    ) {
        context.startActivity(
            Intent(context, activityClass)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .apply(intentSetup)
        )
    }

    /** Leaves nothing behind to steal window focus from whichever test runs next. */
    fun finishAll() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val monitor = ActivityLifecycleMonitorRegistry.getInstance()
            listOf(Stage.RESUMED, Stage.PAUSED, Stage.STOPPED, Stage.STARTED, Stage.CREATED)
                .flatMap { monitor.getActivitiesInStage(it) }
                .forEach { it.finish() }
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }
}
