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
        @PrimaryKey(autoGenerate = true) var id: Int,
        @ColumnInfo(name = "teamID") var teamId: Int,
        @ColumnInfo(name = "name") var name: String,
        @ColumnInfo(name = "shirtNumber") var shirtNumber: Int,
        @ColumnInfo(name = "licenseNumber") var licenseNumber: Long)