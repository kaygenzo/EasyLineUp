package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.telen.easylineup.repository.model.export.LineupExport
import com.telen.easylineup.repository.model.export.TournamentExport
import java.io.Serializable
import java.util.*

@Entity(
        tableName = "tournaments"
)
data class Tournament (
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "createdAt") var createdAt: Long,
        @ColumnInfo(name = "hash") var hash: String? = null
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tournament

        if (id != other.id) return false
        if (name != other.name) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}

fun Tournament.toTournamentExport(lineups: List<LineupExport>): TournamentExport {
    return TournamentExport(hash ?: UUID.randomUUID().toString(), name, createdAt, lineups)
}