package com.telen.easylineup.repository.model

import androidx.room.*
import com.telen.easylineup.repository.model.export.LineupExport
import com.telen.easylineup.repository.model.export.PlayerPositionExport
import java.util.*

const val MODE_NONE = 0
const val MODE_DH = 1

@Entity(
        tableName = "lineups",
        indices = [Index(value = ["name"])],
        foreignKeys = [
            ForeignKey(entity = Team::class, parentColumns = ["id"], childColumns = ["teamID"],
                    onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = Tournament::class, parentColumns = ["id"], childColumns = ["tournamentID"],
                    onDelete = ForeignKey.CASCADE)
        ]
)
data class Lineup(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "name") var name: String = "",
        @ColumnInfo(name = "teamID") var teamId: Long = 0,
        @ColumnInfo(name = "tournamentID") var tournamentId: Long = 0,
        @ColumnInfo(name = "mode") var mode: Int = 0,
        @ColumnInfo(name = "createdAt") var createdTimeInMillis: Long = Calendar.getInstance().timeInMillis,
        @ColumnInfo(name = "editedAt") var editedTimeInMillis: Long = Calendar.getInstance().timeInMillis,
        @ColumnInfo(name = "roaster") var roaster: String? = null,
        @ColumnInfo(name = "hash") var hash: String? = null,
        @Ignore val playerPositions: MutableList<FieldPosition> = mutableListOf()) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Lineup

        if (id != other.id) return false
        if (name != other.name) return false
        if (teamId != other.teamId) return false
        if (tournamentId != other.tournamentId) return false
        if (mode != other.mode) return false
        if (roaster != other.roaster) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + teamId.hashCode()
        result = 31 * result + tournamentId.hashCode()
        result = 31 * result + mode
        result = 31 * result + (roaster?.hashCode() ?: 0)
        return result
    }
}

fun Lineup.toLineupExport(playerPositions: MutableList<PlayerPositionExport>): LineupExport {
    return LineupExport(hash ?: UUID.randomUUID().toString(), name, createdTimeInMillis, editedTimeInMillis, mode, roaster, playerPositions)
}