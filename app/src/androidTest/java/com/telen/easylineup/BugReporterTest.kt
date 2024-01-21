/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import android.app.Activity
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.adevinta.android.barista.interaction.BaristaEditTextInteractions
import com.adevinta.android.barista.interaction.BaristaKeyboardInteractions
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@LargeTest
@RunWith(AndroidJUnit4::class)
class BugReporterTest {
    private var currentDisplayedActivity: Activity? = null
    private val lifecycleCallback: ActivityLifecycleCallback = ActivityLifecycleCallback { activity, stage ->
        if (stage === Stage.RESUMED) {
            Timber.e("RESUMED: ${activity.javaClass.simpleName}")
            currentDisplayedActivity = activity
        }
    }

    @Rule
    @JvmField
    var homeTestRule = EasyLineupActivityTestRule(HomeActivity::class.java, initialTouchMode = true,
        launchActivity = false)

    @Before
    fun init() {
        homeTestRule.launchActivity(Intent())
        monitorCurrentActivity()
    }

    @After
    fun clear() {
        stopMonitorCurrentActivity()
    }

    @Test
    fun testReportSending() {
        // click on report issue action button
        onView(withId(R.id.action_report_issue))
            .perform(click())

        Thread.sleep(2_000)

        // add description Test
        BaristaEditTextInteractions.writeTo(R.id.bugReporterDescription, "Test")
        // close keyboard
        BaristaKeyboardInteractions.closeKeyboard()

        // enter edit screenshot view
        onView(withId(R.id.bugReporterScreenshotPreview))
            .perform(click())

        // click okay action
        onView(withId(R.id.paintActionCheck))
            .perform(click())

        // send report
        onView(withId(R.id.action_send_report))
            .perform(click())
    }

    private fun monitorCurrentActivity() {
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(lifecycleCallback)
    }

    private fun stopMonitorCurrentActivity() {
        ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(lifecycleCallback)
    }
}
