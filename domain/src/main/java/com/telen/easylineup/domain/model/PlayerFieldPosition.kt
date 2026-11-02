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
    val id: Long = 0,
    val playerId: Long = 0,
    val lineupId: Long = 0,
    val position: Int = -1,
    val x: Float = 0f,
    val y: Float = 0f,
    val order: Int = 0,
    val flags: Int = 0,
    val hash: String? = UUID.randomUUID().toString()
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
