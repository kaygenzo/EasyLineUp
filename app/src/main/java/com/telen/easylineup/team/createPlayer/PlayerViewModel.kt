package com.telen.easylineup.team.createPlayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.data.Player
import io.reactivex.Completable

class PlayerViewModel: ViewModel() {

    var playerID: Long? = 0
    var teamID: Long? = 0

    fun savePlayer(player: Player): Completable {
        if(player.id == 0L) {
            return App.database.playerDao().insertPlayer(player)
        }
        else {
            return App.database.playerDao().updatePlayer(player)
        }
    }

    fun getPlayer(playerID: Long): LiveData<Player> {
        return App.database.playerDao().getPlayerById(playerID)
    }
}