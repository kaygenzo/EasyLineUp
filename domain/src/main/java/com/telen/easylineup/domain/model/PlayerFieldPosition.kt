/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.export.PlayerPositionExport
import java.util.UUID

/**
 * @property id
 * @property playerId
 * @property lineupId
 * @property position
 * @property x
 * @property y
 * @property order
 * @property flags
 * @property hash
 */
data class PlayerFieldPosition(
    var id: Long = 0,
    var playerId: Long = 0,
    var lineupId: Long = 0,
    var position: Int = -1,
    var x: Float = 0f,
    var y: Float = 0f,
    var order: Int = 0,
    var flags: Int = 0,
    var hash: String? = UUID.randomUUID().toString()
) {
    companion object {
        const val FLAG_FLEX = 0x01
        const val FLAG_NONE = 0x00
    }
}

fun PlayerFieldPosition.toPlayerFieldPositionsExport(playerUuid: String?): PlayerPositionExport {
    return PlayerPositionExport(
        hash ?: UUID.randomUUID().toString(),
        playerUuid,
        position,
        x,
        y,
        flags,
        order
    )
}
