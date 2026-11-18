/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import android.os.Build
import android.view.ViewGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Keeps a window's first, automatic focus away from its text fields before Android 9.
 *
 * Up to Android 8.1, a window that opens hands its first focusable view the focus, even in
 * touch mode. That view is the Compose view, which passes the focus on to a text field, and
 * the keyboard came up on its own: over the lineup creation screen, the edit-lineup and
 * new-tournament sheets, the roster search. It covered the sheet's rows and ate the next back
 * press. Android 9 stopped giving that focus in touch mode.
 *
 * Here the Compose view's parent takes that first focus itself instead (the framework offers it
 * the focus before its children), so no text field gets it. A tap on a field still focuses it
 * and opens the keyboard: the parent only goes first, it doesn't block its children.
 *
 * Call it once in each window that holds a text field: [DugoutTheme] does it for every
 * Activity's content, and each sheet with a text field does it for its own window.
 */
@Composable
fun KeepInitialFocusOffTextFields() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return
    }
    val view = LocalView.current
    // During the window's first composition, so before its first layout hands out the focus.
    remember(view) {
        (view.parent as? ViewGroup)?.apply {
            descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
            isFocusableInTouchMode = true
        }
    }
}

@DugoutPreviews
@Composable
private fun KeepInitialFocusOffTextFieldsPreview() {
    DugoutTheme {
        KeepInitialFocusOffTextFields()
        Text("No automatic focus on a text field before Android 9")
    }
}
