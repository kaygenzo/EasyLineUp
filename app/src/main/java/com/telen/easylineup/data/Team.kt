package com.telen.easylineup.data

import androidx.room.*

@Entity(
        tableName = "teams",
        indices = [Index(value = ["name"])]
)
data class Team(
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        @ColumnInfo(name = "name") var name: String = "",
        @ColumnInfo(name = "image") var image: String? = null,
        @Ignore val players: MutableList<Player> = mutableListOf()) {

    fun setPlayers(players: List<Player>) {
        this.players.clear()
        this.players.addAll(players)
    }

    override fun toString(): String {
        val builder = StringBuffer().apply {
            append("Team {")
            append("id=$id,")
            append("name=$name,")
            append("image=$image,")
            append("players:")
            players.forEach {
                append(it)
                append(",")
            }
            append("}")
        }
        return builder.toString()
    }
}
