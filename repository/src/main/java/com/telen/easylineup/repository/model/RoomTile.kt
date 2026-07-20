/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.tiles.TileType
import java.io.Serializable

/**
 * @property id
 * @property position
 * @property type
 * @property enabled
 */
@Entity(
    tableName = "tiles",
    indices = [Index(value = ["position"])]
)
internal data class RoomTile(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "position") var position: Int = 0,
    @ColumnInfo(name = "type") var type: Int = TileType.BETA.type,
    @ColumnInfo(name = "enabled") var enabled: Boolean = true
) : Serializable

internal fun RoomTile.toDomain(): DashboardTile {
    return DashboardTile(id, position, type, enabled)
}

internal fun DashboardTile.toRoom(): RoomTile {
    return RoomTile(
        id = id,
        position = position,
        type = type,
        enabled = enabled
    )
}
