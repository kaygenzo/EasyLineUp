/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.ports

/** The current time, injected so the rules that stamp or default to "now" can be tested. */
fun interface Clock {
    fun nowMillis(): Long
}
