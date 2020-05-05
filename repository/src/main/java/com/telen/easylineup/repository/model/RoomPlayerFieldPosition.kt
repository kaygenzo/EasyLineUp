package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.telen.easylineup.domain.model.PlayerFieldPosition
import java.util.*

@Entity(
        tableName = "playerFieldPosition",
        foreignKeys = [
            ForeignKey(entity = RoomPlayer::class, parentColumns = ["id"], childColumns = ["playerID"],
                    onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = RoomLineup::class, parentColumns = ["id"], childColumns = ["lineupID"],
                    onDelete = ForeignKey.CASCADE)
        ]
)
internal data class RoomPlayerFieldPosition(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "playerID") var playerId: Long = 0,
        @ColumnInfo(name = "lineupID") var lineupId: Long = 0,
        @ColumnInfo(name = "position") var position: Int = 0,
        @ColumnInfo(name = "x") var x: Float = 0f,
        @ColumnInfo(name = "y") var y: Float = 0f,
        @ColumnInfo(name = "order") var order: Int = 0,
        @ColumnInfo(name = "flags") var flags: Int = 0,
        @ColumnInfo(name = "hash") var hash: String? = UUID.randomUUID().toString()
)

internal fun RoomPlayerFieldPosition.init(playerFieldPosition: PlayerFieldPosition): RoomPlayerFieldPosition {
    id = playerFieldPosition.id
    playerId = playerFieldPosition.playerId
    lineupId = playerFieldPosition.lineupId
    position = playerFieldPosition.position
    x = playerFieldPosition.x
    y = playerFieldPosition.y
    order = playerFieldPosition.order
    flags = playerFieldPosition.flags
    hash =  playerFieldPosition.hash
    return this
}

internal fun RoomPlayerFieldPosition.toPlayerFieldPosition(): PlayerFieldPosition {
    return PlayerFieldPosition(id, playerId, lineupId, position, x, y, order, flags, hash)
}