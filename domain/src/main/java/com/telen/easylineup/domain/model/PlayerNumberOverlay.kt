/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.export.PlayerNumberOverlayExport
import java.io.Serializable
import java.util.UUID

/**
 * @property id
 * @property lineupId
 * @property playerId
 * @property number
 * @property hash
 */
data class PlayerNumberOverlay(
    var id: Long = 0,
    var lineupId: Long = 0,
    var playerId: Long = 0,
    var number: Int = 0,
    var hash: String? = UUID.randomUUID().toString()
) : Serializable

fun PlayerNumberOverlay.toPlayerNumberOverlayExport(playerUuid: String?):
PlayerNumberOverlayExport {
    return PlayerNumberOverlayExport(hash ?: UUID.randomUUID().toString(), playerUuid, number)
}
