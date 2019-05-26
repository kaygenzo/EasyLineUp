package com.telen.easylineup.team.createPlayer

import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.data.Player
import io.reactivex.Completable

class PlayerViewModel: ViewModel() {

    fun savePlayer(player: Player): Completable {
        return App.database.playerDao().insertPlayer(player)
    }
}