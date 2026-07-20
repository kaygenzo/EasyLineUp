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
@Entity(
    tableName = "playerFieldPosition",
    indices = [Index(value = ["playerID", "lineupID"])],
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
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "playerID") var playerId: Long = 0,
    @ColumnInfo(name = "lineupID") var lineupId: Long = 0,
    @ColumnInfo(name = "position") var position: Int = -1,
    @ColumnInfo(name = "x") var x: Float = 0f,
    @ColumnInfo(name = "y") var y: Float = 0f,
    @ColumnInfo(name = "order") var order: Int = 0,
    @ColumnInfo(name = "flags") var flags: Int = 0,
    @ColumnInfo(name = "hash") var hash: String? = UUID.randomUUID().toString()
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
