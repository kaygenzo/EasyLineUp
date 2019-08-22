package com.telen.easylineup.team.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.data.PositionWithLineup
import io.reactivex.Completable

class PlayerDetailsViewModel: ViewModel() {

    var playerID: Long? = null

    fun getAllLineupsForPlayer(playerID: Long): LiveData<List<PositionWithLineup>> {
        return App.database.lineupDao().getAllPositionsForPlayer(playerID)
    }

    fun deletePlayer(): Completable {
        playerID?.let {id ->
            return App.database.playerDao().getPlayerByIdAsSingle(id)
                    .flatMapCompletable { player -> App.database.playerDao().deletePlayer(player) }
        } ?: return Completable.complete()
    }
}