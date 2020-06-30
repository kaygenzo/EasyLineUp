package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.tiles.TileType
import java.io.Serializable
import java.util.*

@Entity(
        tableName = "tiles",
        indices = [Index(value = ["position"])]
)
internal data class RoomTile(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "position") var position: Int = 0,
        @ColumnInfo(name = "type") var type: Int = TileType.BETA.type,
        @ColumnInfo(name = "enabled") var enabled: Boolean = true
): Serializable

internal fun RoomTile.toDashboardTile(): DashboardTile {
    return DashboardTile(id, position, type, enabled)
}

internal fun RoomTile.init(tile: DashboardTile): RoomTile {
    id = tile.id
    position = tile.position
    type = tile.type
    enabled = tile.enabled
    return this
}