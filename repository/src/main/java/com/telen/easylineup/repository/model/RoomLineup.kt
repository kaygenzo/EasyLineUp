package com.telen.easylineup.repository.model

import androidx.room.*
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import java.util.*

@Entity(
        tableName = "lineups",
        indices = [Index(value = ["name", "teamID", "tournamentID"])],
        foreignKeys = [
            ForeignKey(entity = RoomTeam::class, parentColumns = ["id"], childColumns = ["teamID"],
                    onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = RoomTournament::class, parentColumns = ["id"], childColumns = ["tournamentID"],
                    onDelete = ForeignKey.CASCADE)
        ]
)
internal data class RoomLineup(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "name") var name: String = "",
        @ColumnInfo(name = "teamID") var teamId: Long = 0,
        @ColumnInfo(name = "tournamentID") var tournamentId: Long = 0,
        @ColumnInfo(name = "mode") var mode: Int = 0,
        @ColumnInfo(name = "createdAt") var createdTimeInMillis: Long = Calendar.getInstance().timeInMillis,
        @ColumnInfo(name = "editedAt") var editedTimeInMillis: Long = Calendar.getInstance().timeInMillis,
        @ColumnInfo(name = "roaster") var roster: String? = null,
        @ColumnInfo(name = "hash") var hash: String? = UUID.randomUUID().toString(),
        @Ignore val playerPositions: MutableList<FieldPosition> = mutableListOf()) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomLineup

        if (id != other.id) return false
        if (name != other.name) return false
        if (teamId != other.teamId) return false
        if (tournamentId != other.tournamentId) return false
        if (mode != other.mode) return false
        if (roster != other.roster) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + teamId.hashCode()
        result = 31 * result + tournamentId.hashCode()
        result = 31 * result + mode
        result = 31 * result + (roster?.hashCode() ?: 0)
        return result
    }
}

internal fun RoomLineup.init(lineup: Lineup): RoomLineup {
    id = lineup.id
    name = lineup.name
    teamId = lineup.teamId
    tournamentId = lineup.tournamentId
    mode = lineup.mode
    createdTimeInMillis = lineup.createdTimeInMillis
    editedTimeInMillis = lineup.editedTimeInMillis
    roster = lineup.roster
    hash = lineup.hash
    playerPositions.apply {
        clear()
        addAll(lineup.playerPositions)
    }
    return this
}

internal fun RoomLineup.toLineup(): Lineup {
    return Lineup(id, name, teamId, tournamentId, mode, createdTimeInMillis, editedTimeInMillis, roster, hash, playerPositions)
}