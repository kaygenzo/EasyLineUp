/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import java.io.Serializable
import java.util.UUID

/**
 * @property id
 * @property lineupId
 * @property playerId
 * @property number
 * @property hash
 */
@Entity(
    tableName = "playerNumberOverlay",
    indices = [Index(value = ["number"])],
    foreignKeys = [
        ForeignKey(entity = RoomPlayer::class, parentColumns = ["id"], childColumns = ["playerID"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = RoomLineup::class, parentColumns = ["id"], childColumns = ["lineupID"],
            onDelete = ForeignKey.CASCADE)
    ]
)
internal data class RoomPlayerNumberOverlay(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "lineupID") var lineupId: Long = 0,
    @ColumnInfo(name = "playerID") var playerId: Long = 0,
    @ColumnInfo(name = "number") var number: Int = 0,
    @ColumnInfo(name = "hash") var hash: String? = UUID.randomUUID().toString()
) : Serializable

internal fun RoomPlayerNumberOverlay.toPlayerNumberOverlay(): PlayerNumberOverlay {
    return PlayerNumberOverlay(id, lineupId, playerId, number, hash)
}

internal fun RoomPlayerNumberOverlay.init(overlay: PlayerNumberOverlay): RoomPlayerNumberOverlay {
    id = overlay.id
    lineupId = overlay.lineupId
    playerId = overlay.playerId
    number = overlay.number
    hash = overlay.hash
    return this
}
