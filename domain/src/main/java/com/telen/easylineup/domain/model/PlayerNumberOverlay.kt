package com.telen.easylineup.domain.model

import java.io.Serializable

data class PlayerNumberOverlay(
        var id: Long = 0,
        var lineupID: Long = 0,
        var playerID: Long = 0,
        var number: Int = 0
): Serializable