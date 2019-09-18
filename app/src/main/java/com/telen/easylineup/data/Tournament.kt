package com.telen.easylineup.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
        tableName = "tournaments"
)
data class Tournament(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "createdAt") var createdAt: Long
)