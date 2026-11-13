/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import java.util.Locale

/**
 * Upper case by the rules of the app's language, for a translated label shown in capitals.
 * Kotlin's own `uppercase()` uses no language at all: in Turkish, "i" would become "I" instead
 * of "İ".
 */
fun String.uppercaseLocalized(): String = uppercase(Locale.getDefault())
