/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import com.telen.easylineup.domain.model.ShirtNumberEntry

/**
 * @property number
 * @property playerName
 * @property playerId
 * @property eventTime
 * @property createdAt
 * @property lineupId
 * @property lineupName
 */
internal data class RoomShirtNumberEntry(
    @ColumnInfo(name = "number") val number: Int,
    @ColumnInfo(name = "playerName") val playerName: String,
    @ColumnInfo(name = "playerID") val playerId: Long,
    @ColumnInfo(name = "time") val eventTime: Long,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "lineupID") val lineupId: Long,
    @ColumnInfo(name = "lineupName") val lineupName: String
)

internal fun RoomShirtNumberEntry.toShirtNumberEntry(): ShirtNumberEntry {
    return ShirtNumberEntry(number, playerName, playerId, eventTime, createdAt, lineupId, lineupName)
}
