/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

/**
 * Rotates the device under test via [UiDevice] - the modern, Compose-compatible replacement
 * for the old (deleted) Espresso `OrientationChangeAction`.
 */
object OrientationTestUtils {
    // Rotation is asynchronous at the window-manager level - UiDevice's own waitForIdle can
    // report idle before the Activity has actually finished its configuration-change
    // recreate/re-layout, so give it a generous explicit timeout rather than the default.
    private const val SETTLE_TIMEOUT_MILLIS = 2_000L

    private val device: UiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun rotateToLandscape() {
        device.setOrientationLeft()
        device.waitForIdle(SETTLE_TIMEOUT_MILLIS)
    }

    fun rotateToPortrait() {
        device.setOrientationNatural()
        device.waitForIdle(SETTLE_TIMEOUT_MILLIS)
    }
}
