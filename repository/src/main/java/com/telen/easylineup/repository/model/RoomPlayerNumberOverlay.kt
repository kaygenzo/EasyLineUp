package com.telen.easylineup.repository.model

import androidx.room.*
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import java.io.Serializable

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
        @ColumnInfo(name = "lineupID") var lineupID: Long = 0,
        @ColumnInfo(name = "playerID") var playerID: Long = 0,
        @ColumnInfo(name = "number") var number: Int = 0
): Serializable

internal fun RoomPlayerNumberOverlay.toPlayerNumberOverlay(): PlayerNumberOverlay {
    return PlayerNumberOverlay(id, lineupID, playerID, number)
}

internal fun RoomPlayerNumberOverlay.init(overlay: PlayerNumberOverlay): RoomPlayerNumberOverlay {
    id = overlay.id
    lineupID = overlay.lineupID
    playerID = overlay.playerID
    number = overlay.number
    return this
}