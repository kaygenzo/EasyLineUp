package com.telen.easylineup.player

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.PositionWithLineup
import io.reactivex.Completable

class PlayerViewModel: ViewModel() {

    var playerID: Long? = 0
    var teamID: Long? = 0

    fun savePlayer(name: String, shirtNumber: Int, licenseNumber: Long, imageUri: Uri?, positions: Int): Completable {

        val playerID: Long = playerID ?: 0
        val teamID: Long = teamID ?: 0
        val player = Player(id = playerID, teamId = teamID, name = name, shirtNumber = shirtNumber,
                licenseNumber = licenseNumber, image = imageUri?.toString(), positions = positions)

        return if(player.id == 0L) {
            App.database.playerDao().insertPlayer(player)
        }
        else {
            App.database.playerDao().updatePlayer(player)
        }
    }

    fun getAllLineupsForPlayer(): LiveData<Map<FieldPosition, Int>> {
        playerID?.let {
            return Transformations.map(App.database.lineupDao().getAllPositionsForPlayer(it)) { list ->
                val chartData: MutableMap<FieldPosition, Int> = mutableMapOf()
                list.forEach { position ->
                    val fieldPosition = FieldPosition.getFieldPosition(position.position)
                    fieldPosition?.let { element ->
                        chartData[element] = chartData[element]?.let { it + 1 } ?: 1
                    }
                }
                chartData
            }
        } ?: throw IllegalStateException()
    }

    fun deletePlayer(): Completable {
        playerID?.let {id ->
            return App.database.playerDao().getPlayerByIdAsSingle(id)
                    .flatMapCompletable { player -> App.database.playerDao().deletePlayer(player) }
        } ?: return Completable.complete()
    }

    fun getPlayer(): LiveData<Player> {
        playerID?.let {
            return App.database.playerDao().getPlayerById(it)
        } ?: throw IllegalStateException()
    }
}