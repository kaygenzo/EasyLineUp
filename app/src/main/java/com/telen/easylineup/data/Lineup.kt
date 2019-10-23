package com.telen.easylineup.data

import androidx.room.*
import com.telen.easylineup.FieldPosition
import java.util.*

const val MODE_NONE = 0
const val MODE_DH = 1

@Entity(
        tableName = "playerFieldPosition",
        foreignKeys = [
            ForeignKey(entity = Player::class, parentColumns = ["id"], childColumns = ["playerID"],
                    onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = Lineup::class, parentColumns = ["id"], childColumns = ["lineupID"],
                    onDelete = ForeignKey.CASCADE)
        ]
)
data class PlayerFieldPosition(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "playerID") var playerId: Long = 0,
        @ColumnInfo(name = "lineupID") var lineupId: Long = 0,
        @ColumnInfo(name = "position") var position: Int = 0,
        @ColumnInfo(name = "x") var x: Float = 0f,
        @ColumnInfo(name = "y") var y: Float = 0f,
        @ColumnInfo(name = "order") var order: Int = 0
)

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
        @Ignore val playerPositions: MutableList<FieldPosition> = mutableListOf())

data class PlayerWithPosition(
        @ColumnInfo(name = "playerName") val playerName: String,
        @ColumnInfo(name = "shirtNumber") val shirtNumber: Int,
        @ColumnInfo(name = "licenseNumber") val licenseNumber: Long,
        @ColumnInfo(name = "teamID") val teamId: Long,
        @ColumnInfo(name = "image") val image: String?,
        @ColumnInfo(name = "position") var position: Int = 0,
        @ColumnInfo(name = "x") var x: Float = 0f,
        @ColumnInfo(name = "y") var y: Float = 0f,
        @ColumnInfo(name = "order") var order: Int = 0,
        @ColumnInfo(name = "fieldPositionID") var fieldPositionID: Long = 0,
        @ColumnInfo(name = "playerID") val playerID: Long,
        @ColumnInfo(name = "lineupID") val lineupId: Long,
        @ColumnInfo(name = "playerPositions") val playerPositions: Int
) {
    fun toPlayer(): Player {
        return Player(id = playerID, teamId = teamId, name = playerName, shirtNumber = shirtNumber, licenseNumber = licenseNumber, image = image, positions = playerPositions)
    }

    fun toPlayerFieldPosition(): PlayerFieldPosition {
        return PlayerFieldPosition(id = fieldPositionID, playerId = playerID, position = position, x = x, y = y, order = order, lineupId = lineupId)
    }
}

data class PositionWithLineup(
        @ColumnInfo(name = "position") var position: Int = 0,
        @ColumnInfo(name = "x") var x: Float = 0f,
        @ColumnInfo(name = "y") var y: Float = 0f,
        @ColumnInfo(name = "order") var order: Int = 0,
        @ColumnInfo(name = "lineupName") var lineupName: String = "",
        @ColumnInfo(name = "tournamentName") var tournamentName: String = ""
)

data class TournamentWithLineup(
        @ColumnInfo(name = "tournamentID") var tournamentID: Long = 0,
        @ColumnInfo(name = "tournamentName") var tournamentName: String = "",
        @ColumnInfo(name = "tournamentCreatedAt") var tournamentCreatedAt: Long = 0,
        @ColumnInfo(name = "fieldPositionID") var fieldPositionID: Long = 0,
        @ColumnInfo(name = "lineupName") var lineupName: String? = "",
        @ColumnInfo(name = "lineupID") var lineupID: Long = 0,
        @ColumnInfo(name = "x") var x: Float = 0f,
        @ColumnInfo(name = "y") var y: Float = 0f,
        @ColumnInfo(name = "position") var position: Int = 0
) {
    fun toTournament() : Tournament {
        return Tournament(id = tournamentID, name = tournamentName, createdAt = tournamentCreatedAt)
    }

    fun toLineup(): Lineup {
        return Lineup(id = lineupID, name = lineupName ?: "", tournamentId = tournamentID)
    }
}

data class PlayerGamesCount (
        @ColumnInfo(name = "playerID") var playerID: Long = 0,
        @ColumnInfo(name = "size") var size: Int = 0
)