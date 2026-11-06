/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.test.platform.app.InstrumentationRegistry

private val targetContext: Context
    get() = InstrumentationRegistry.getInstrumentation().targetContext

/**
 * Resolves a string the same way the app does, in the device's language - instrumented tests look
 * text up through this rather than a hardcoded English literal, so they pass on a device in any
 * language the app is translated to and survive a wording change. Text rendered with
 * `.uppercase()` by the screen needs the same `.uppercase()` here.
 */
fun string(@StringRes id: Int, vararg formatArgs: Any): String =
    targetContext.getString(id, *formatArgs)

fun pluralString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String =
    targetContext.resources.getQuantityString(id, quantity, *formatArgs)
