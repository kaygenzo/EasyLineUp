package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
        tableName = "training_cards",
        indices = [Index(value = ["title"])]
)
internal data class TrainingCard(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "title") var title: String = "",
        @ColumnInfo(name = "description") var description: String,
        @ColumnInfo(name = "level") var level: Int = 0,
        @ColumnInfo(name = "duration") var duration: Long = 0): Serializable {

    override fun toString(): String {
        return "TrainingCard(id=$id, title='$title', description='$description', level=$level, duration=$duration)"
    }
}