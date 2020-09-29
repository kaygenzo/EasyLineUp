package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.export.PlayerNumberOverlayExport
import java.io.Serializable
import java.util.*

data class PlayerNumberOverlay(
        var id: Long = 0,
        var lineupID: Long = 0,
        var playerID: Long = 0,
        var number: Int = 0,
        var hash: String? = UUID.randomUUID().toString()
): Serializable

fun PlayerNumberOverlay.toPlayerNumberOverlayExport(playerUUID: String?): PlayerNumberOverlayExport {
    return PlayerNumberOverlayExport(hash ?: UUID.randomUUID().toString(), playerUUID, number)
}