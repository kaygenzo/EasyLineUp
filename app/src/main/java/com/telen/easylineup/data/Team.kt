package com.telen.easylineup.data

import androidx.room.*

enum class TeamType(val id: Int, val position: Int) {
    UNKNOWN(0, -1),
    BASEBALL(1, 0),
    SOFTBALL(2, 1);

    companion object {
        fun getTypeById(id: Int): TeamType {
            values().forEach {
                if(it.id == id)
                    return it
            }
            return UNKNOWN
        }
    }
}

@Entity(
        tableName = "teams",
        indices = [Index(value = ["name"])]
)
data class Team(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "name") var name: String = "",
        @ColumnInfo(name = "image") var image: String? = null,
        @ColumnInfo(name = "type") val type: Int) {

    override fun toString(): String {
        val builder = StringBuffer().apply {
            append("Team {")
            append("id=$id,")
            append("name=$name,")
            append("image=$image,")
            append("type=$type")
            append("}")
        }
        return builder.toString()
    }
}
