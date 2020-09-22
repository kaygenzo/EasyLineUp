package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import com.telen.easylineup.domain.model.ShirtNumberEntry

internal data class RoomShirtNumberEntry(
        @ColumnInfo(name = "number") val number: Int,
        @ColumnInfo(name = "playerName") val playerName: String,
        @ColumnInfo(name = "time") val eventTime: Long,
        @ColumnInfo(name = "createdAt") val createdAt: Long,
        @ColumnInfo(name = "lineupID") val lineupID: Long
)

internal fun RoomShirtNumberEntry.toShirtNumberEntry(): ShirtNumberEntry {
    return ShirtNumberEntry(number, playerName, eventTime, createdAt, lineupID)
}