package com.telen.easylineup.data

import androidx.room.*

@Entity(
        tableName = "players",
        indices = [Index(value = ["name", "licenseNumber"])],
        foreignKeys = [
            ForeignKey(entity = Team::class, parentColumns = ["id"], childColumns = ["teamID"],
                    onDelete = ForeignKey.CASCADE)
        ]
)
data class Player(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "teamID") var teamId: Long,
        @ColumnInfo(name = "name") var name: String,
        @ColumnInfo(name = "shirtNumber") var shirtNumber: Int,
        @ColumnInfo(name = "licenseNumber") var licenseNumber: Long,
        @ColumnInfo(name = "image") var image: String? = null
        ,@ColumnInfo(name = "positions") var positions: Int = 0
)