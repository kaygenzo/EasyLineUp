package com.telen.easylineup.data

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class DatabaseMockProvider {
    private var players = MutableLiveData<List<Player>>()
    private var team = MutableLiveData<Team>()

    fun retrievePlayers(): LiveData<List<Player>> {
        players.value = getMockPlayers()
        return players
    }

    fun retrieveTeam(): LiveData<Team> {
        this.team.value = getMockTeam()
        return this.team
    }

    private fun getMockTeam(): Team {
        val team = Team("Seadogs", null)
        team.setPlayers(getMockPlayers())
        return team
    }

    private fun getMockPlayers() : List<Player> {
        var playersList = ArrayList<Player>()
        playersList.add(Player("Karim", 20, 1))
        playersList.add(Player("Valentin", 90, 2))
        playersList.add(Player("Antoine", 10, 3))
        playersList.add(Player("Vincent", 24, 4))
        playersList.add(Player("Angie", 1, 5))
        playersList.add(Player("Cyrille", 2, 6))
        playersList.add(Player("JM", 3, 7))
        playersList.add(Player("Lya", 4, 8))
        return playersList
    }
}