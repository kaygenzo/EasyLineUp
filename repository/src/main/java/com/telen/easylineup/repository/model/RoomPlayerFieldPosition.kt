/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.telen.easylineup.domain.model.PlayerFieldPosition

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
@Entity(
    tableName = "playerFieldPosition",
    indices = [
        Index(value = ["playerID", "lineupID"]),
        Index(value = ["lineupID"]),
        Index(value = ["hash"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = RoomPlayer::class, parentColumns = ["id"], childColumns = ["playerID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomLineup::class, parentColumns = ["id"], childColumns = ["lineupID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class RoomPlayerFieldPosition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "playerID") val playerId: Long = 0,
    @ColumnInfo(name = "lineupID") val lineupId: Long = 0,
    @ColumnInfo(name = "position") val position: Int = -1,
    @ColumnInfo(name = "x") val x: Float = 0f,
    @ColumnInfo(name = "y") val y: Float = 0f,
    @ColumnInfo(name = "order") val order: Int = 0,
    @ColumnInfo(name = "flags") val flags: Int = 0,
    @ColumnInfo(name = "hash") val hash: String? = null
)

internal fun PlayerFieldPosition.toRoom(): RoomPlayerFieldPosition {
    return RoomPlayerFieldPosition(
        id = id,
        playerId = playerId,
        lineupId = lineupId,
        position = position,
        x = x,
        y = y,
        order = order,
        flags = flags,
        hash = hash
    )
}

internal fun RoomPlayerFieldPosition.toDomain(): PlayerFieldPosition {
    return PlayerFieldPosition(
        id = id,
        playerId = playerId,
        lineupId = lineupId,
        position = position,
        x = x,
        y = y,
        order = order,
        flags = flags,
        hash = hash
    )
}
