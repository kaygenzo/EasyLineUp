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
    private val device: UiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun rotateToLandscape() {
        device.setOrientationLeft()
        device.waitForIdle()
    }

    fun rotateToPortrait() {
        device.setOrientationNatural()
        device.waitForIdle()
    }
}
