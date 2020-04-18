package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.telen.easylineup.repository.model.export.PlayerExport
import com.telen.easylineup.repository.model.export.TeamExport
import com.telen.easylineup.repository.model.export.TournamentExport
import java.io.Serializable
import java.util.*

@Entity(
        tableName = "teams",
        indices = [Index(value = ["name"])]
)
data class Team(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "name") var name: String = "",
        @ColumnInfo(name = "image") var image: String? = null,
        @ColumnInfo(name = "type") var type: Int = 0,
        @ColumnInfo(name = "main") var main: Boolean = false,
        @ColumnInfo(name = "hash") var hash: String? = UUID.randomUUID().toString()): Serializable {

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
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Team

        if (id != other.id) return false
        if (name != other.name) return false
        if (image != other.image) return false
        if (type != other.type) return false
        if (main != other.main) return false

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

fun Team.toTeamExport(players: List<PlayerExport>, tournaments: List<TournamentExport>) : TeamExport {
    return TeamExport(hash ?: UUID.randomUUID().toString(), name, image, type, main, players, tournaments)
}