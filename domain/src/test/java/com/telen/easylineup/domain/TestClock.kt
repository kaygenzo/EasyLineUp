/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.ports.Clock

/** A [Clock] stopped at [nowMillis], so a "now" a use case stamps can be asserted exactly. */
internal fun fixedClock(nowMillis: Long = TEST_NOW): Clock = Clock { nowMillis }

internal const val TEST_NOW = 1_760_000_000_000L
