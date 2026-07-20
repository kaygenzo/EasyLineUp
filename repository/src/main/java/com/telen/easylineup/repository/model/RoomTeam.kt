/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.telen.easylineup.domain.model.Team
import java.io.Serializable
import java.util.UUID

/**
 * @property id
 * @property name
 * @property image
 * @property type
 * @property main
 * @property hash
 */
@Entity(
    tableName = "teams",
    indices = [Index(value = ["name"])]
)
internal data class RoomTeam(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "image") var image: String? = null,
    @ColumnInfo(name = "type") var type: Int = 0,
    @ColumnInfo(name = "main") var main: Boolean = false,
    @ColumnInfo(name = "hash") var hash: String? = UUID.randomUUID().toString()
) : Serializable {
    override fun toString(): String {
        val builder = StringBuffer().apply {
            append("Team {")
            append("id=$id,")
            append("name=$name,")
            append("image=$image,")
            append("type=$type")
            append("main=$main")
            append("hash=$hash")
            append("}")
        }
        return builder.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as RoomTeam

        if (id != other.id) {
            return false
        }
        if (name != other.name) {
            return false
        }
        if (image != other.image) {
            return false
        }
        if (type != other.type) {
            return false
        }
        if (main != other.main) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + type
        result = 31 * result + main.hashCode()
        return result
    }
}

internal fun Team.toRoom(): RoomTeam {
    return RoomTeam(
        id = id,
        name = name,
        image = image,
        type = type,
        main = main,
        hash = hash
    )
}

internal fun RoomTeam.toDomain(): Team {
    return Team(
        id = id,
        name = name,
        image = image,
        type = type,
        main = main,
        hash = hash
    )
}
