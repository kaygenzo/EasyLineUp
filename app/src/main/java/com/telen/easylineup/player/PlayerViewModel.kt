package com.telen.easylineup.player

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.model.Player
import io.reactivex.Completable
import java.security.InvalidParameterException

enum class FormErrorResult {
    INVALID_NAME,
    INVALID_LICENSE,
    INVALID_NUMBER
}

class PlayerViewModel: ViewModel() {

    private val errorResult = MutableLiveData<FormErrorResult>()

    var playerID: Long? = 0

    fun savePlayer(name: String?, shirtNumber: Int?, licenseNumber: Long?, imageUri: Uri?, positions: Int): Completable {

        if(name == null || name.trim().isEmpty()) {
            errorResult.value = FormErrorResult.INVALID_NAME
            return Completable.error(InvalidParameterException())
        }
        else if(shirtNumber == null) {
            errorResult.value = FormErrorResult.INVALID_NUMBER
            return Completable.error(InvalidParameterException())
        }
        else if(licenseNumber == null) {
            errorResult.value = FormErrorResult.INVALID_LICENSE
            return Completable.error(InvalidParameterException())
        }
        else {
            val playerID: Long = playerID ?: 0
            val teamID = App.prefs.getLong(Constants.PREF_CURRENT_TEAM_ID, 1)
            val player = Player(id = playerID, teamId = teamID, name = name.trim(), shirtNumber = shirtNumber,
                    licenseNumber = licenseNumber, image = imageUri?.toString(), positions = positions)

            return if(player.id == 0L) {
                App.database.playerDao().insertPlayer(player)
            }
            else {
                App.database.playerDao().updatePlayer(player)
            }
        }
    }

    fun getAllLineupsForPlayer(): LiveData<Map<FieldPosition, Int>> {
        playerID?.let {
            return Transformations.map(App.database.playerFieldPositionsDao().getAllPositionsForPlayer(it)) { list ->
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

    fun registerFormErrorResult(): LiveData<FormErrorResult> {
        return errorResult
    }
}