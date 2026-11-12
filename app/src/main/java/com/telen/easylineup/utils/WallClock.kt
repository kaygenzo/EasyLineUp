/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import com.telen.easylineup.domain.ports.Clock

class WallClock : Clock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
