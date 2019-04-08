package com.telen.easylineup.data

import androidx.room.*

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
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "playerID") var playerId: Int,
        @ColumnInfo(name = "lineupID") var lineupId: Int,
        @ColumnInfo(name = "position") var position: Int,
        @ColumnInfo(name = "x") var x: Float,
        @ColumnInfo(name = "y") var y: Float
)

@Entity(
        tableName = "lineups",
        indices = [Index(value = ["name"])],
        foreignKeys = [
            ForeignKey(entity = Team::class, parentColumns = ["id"], childColumns = ["teamID"],
                    onDelete = ForeignKey.CASCADE)
        ]
)
data class Lineup(
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        @ColumnInfo(name = "name") var name: String = "",
        @ColumnInfo(name = "teamID") var teamId: Int = 0,
        @Ignore val playerFieldPosition: MutableList<PlayerFieldPosition> = mutableListOf())