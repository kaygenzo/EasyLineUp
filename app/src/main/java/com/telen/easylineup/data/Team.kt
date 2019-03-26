package com.telen.easylineup.data

data class Team(val name: String, val image: String?) {
    val players: MutableList<Player> by lazy {
        ArrayList<Player>()
    }

    fun setPlayers(players: List<Player>) {
        this.players.clear()
        this.players.addAll(players)
    }

    fun addPlayers(players: List<Player>) {
        this.players.addAll(players)
    }

    fun addPlayer(player: Player) {
        this.players.add(player)
    }

    override fun toString(): String {
        val builder = StringBuffer().apply {
            append("Team {")
            players.forEach {
                append(it)
                append(",")
            }
            append("}")
        }
        return builder.toString()
    }
}
