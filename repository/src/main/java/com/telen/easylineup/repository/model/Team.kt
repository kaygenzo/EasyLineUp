package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
        tableName = "teams",
        indices = [Index(value = ["name"])]
)
data class Team(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "name") var name: String = "",
        @ColumnInfo(name = "image") var image: String? = null,
        @ColumnInfo(name = "type") var type: Int = 0,
        @ColumnInfo(name = "main") var main: Boolean = false): Serializable {

    override fun toString(): String {
        val builder = StringBuffer().apply {
            append("Team {")
            append("id=$id,")
            append("name=$name,")
            append("image=$image,")
            append("type=$type")
            append("main=$main")
            append("}")
        }
        return builder.toString()
    }
}
