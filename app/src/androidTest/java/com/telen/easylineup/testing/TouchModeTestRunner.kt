/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnitRunner
import org.junit.runner.Description
import org.junit.runner.notification.RunListener

/**
 * The app's test runner: AndroidJUnitRunner, with the system put back in touch mode before every
 * test (see [TouchModeListener]).
 *
 * Compose test clicks are dispatched straight to the view, not through the system's input, so
 * they never put the device in touch mode, and any key event (a back press) takes it out of it
 * for every later window. Before Android 9, a window that opens in non-touch mode hands its first
 * focusable view the focus: the lineup name field of the creation screen or of a sheet, and the
 * soft keyboard came up over the sheet's lower rows (the click landed on the keyboard) or ate the
 * next back press. A user's touch puts the device in touch mode, where none of this happens, so
 * the tests run there too.
 */
class TouchModeTestRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle) {
        // The runner's `listener` argument: a comma-separated list of RunListener classes.
        val listeners = listOfNotNull(
            arguments.getString(LISTENER_ARGUMENT),
            TouchModeListener::class.java.name,
        )
        arguments.putString(LISTENER_ARGUMENT, listeners.joinToString(","))
        super.onCreate(arguments)
    }

    private companion object {
        const val LISTENER_ARGUMENT = "listener"
    }
}

/**
 * Enters touch mode as each test starts, before its rules launch an activity: setting it once
 * for the whole run isn't enough, since a test's back press leaves it for all the tests after.
 */
class TouchModeListener : RunListener() {
    override fun testStarted(description: Description) {
        enterTouchMode()
    }
}

/** Puts the system in touch mode, the mode a user's first touch puts it in. */
fun enterTouchMode() {
    InstrumentationRegistry.getInstrumentation().setInTouchMode(true)
}
