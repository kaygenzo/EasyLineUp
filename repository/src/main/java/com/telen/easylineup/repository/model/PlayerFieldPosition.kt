package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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