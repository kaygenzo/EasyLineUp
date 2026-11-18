/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

/**
 * A system back key press, through UiAutomator rather than Espresso's `pressBack()`. Espresso's
 * first waits up to 10 s for the app window to have focus, and on the CI tablet emulator it
 * sometimes never got it right after a sheet or a navigation (RootViewWithoutFocusException),
 * though the back key itself works. The key goes through the window's back dispatch either way,
 * the way a user's does. No `waitForIdleSync()` after it: a screen with an endless animation (the
 * defense markers') never lets the main thread go idle, so it could wait forever; the compose
 * rule's own synchronization, on the next interaction, is what waits for the screen to settle.
 *
 * A key event takes the system out of touch mode, so it goes back in right after (see
 * [TouchModeTestRunner]): otherwise, before Android 9, the next sheet would open with its text
 * field focused and the soft keyboard over it.
 */
fun pressBack() {
    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
    enterTouchMode()
}
