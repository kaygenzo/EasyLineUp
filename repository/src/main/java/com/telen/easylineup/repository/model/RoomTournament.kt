/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.telen.easylineup.domain.model.Tournament
import java.io.Serializable
import java.util.UUID

/**
 * @property id
 * @property name
 * @property createdAt
 * @property startTime
 * @property endTime
 * @property address
 * @property hash
 */
@Entity(
    tableName = "tournaments"
)
internal data class RoomTournament(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "createdAt") var createdAt: Long = 0L,
    @ColumnInfo(name = "startTime") var startTime: Long = 0L,
    @ColumnInfo(name = "endTime") var endTime: Long = 0L,
    @ColumnInfo(name = "address") var address: String? = null,
    @ColumnInfo(name = "hash") var hash: String? = UUID.randomUUID().toString()
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as RoomTournament

        if (id != other.id) {
            return false
        }
        if (name != other.name) {
            return false
        }
        if (createdAt != other.createdAt) {
            return false
        }
        if (startTime != other.startTime) {
            return false
        }
        if (endTime != other.endTime) {
            return false
        }
        if (address != other.address) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        result = 31 * result + (address?.hashCode() ?: 0)
        return result
    }
}

internal fun Tournament.toRoom(): RoomTournament {
    return RoomTournament(
        id = id,
        name = name,
        createdAt = createdAt,
        startTime = startTime,
        endTime = endTime,
        address = address,
        hash = hash
    )
}

internal fun RoomTournament.toDomain(): Tournament {
    return Tournament(
        id = id,
        name = name,
        createdAt = createdAt,
        startTime = startTime,
        endTime = endTime,
        address = address,
        hash = hash
    )
}
