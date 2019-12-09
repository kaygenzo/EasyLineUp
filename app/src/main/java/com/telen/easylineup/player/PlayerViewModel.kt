package com.telen.easylineup.player

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.*
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.model.Player
import io.reactivex.Completable
import io.reactivex.Single

enum class FormErrorResult {
    INVALID_NAME,
    INVALID_LICENSE,
    INVALID_NUMBER
}

class PlayerViewModel: ViewModel() {

    private val errorResult = MutableLiveData<FormErrorResult>()
    private val getTeamUseCase = GetTeam(App.database.teamDao())
    private val getPlayerUseCase = GetPlayer(App.database.playerDao())
    private val deletePlayerUseCase = DeletePlayer(App.database.playerDao())
    private val savePlayerUseCase = SavePlayer(App.database.playerDao())
    private val getPlayerPositionsSummaryUseCase = GetPositionsSummaryForPlayer(App.database.playerFieldPositionsDao())

    var playerID: Long? = 0

    fun savePlayer(name: String?, shirtNumber: Int?, licenseNumber: Long?, imageUri: Uri?, positions: Int): Completable {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMapCompletable {
                    val req = SavePlayer.RequestValues(playerID ?: 0, it.id, name, shirtNumber, licenseNumber, imageUri, positions)
                    UseCaseHandler.execute(savePlayerUseCase, req).ignoreElement()
                }
                .doOnError {
                    when (it) {
                        is NameEmptyException -> errorResult.value = FormErrorResult.INVALID_NAME
                        is ShirtNumberEmptyException -> errorResult.value = FormErrorResult.INVALID_NUMBER
                        is LicenseNumberEmptyException -> errorResult.value = FormErrorResult.INVALID_LICENSE
                    }
                }
    }

    fun getAllLineupsForPlayer(): Single<Map<FieldPosition, Int>> {
        return UseCaseHandler.execute(getPlayerPositionsSummaryUseCase, GetPositionsSummaryForPlayer.RequestValues(playerID)).map { it.summary }
    }

    fun deletePlayer(): Completable {
        return UseCaseHandler.execute(getPlayerUseCase, GetPlayer.RequestValues(playerID)).map { it.player }
                .flatMapCompletable { player -> UseCaseHandler.execute(deletePlayerUseCase, DeletePlayer.RequestValues(player)).ignoreElement() }
    }

    fun getPlayer(): Single<Player> {
        return UseCaseHandler.execute(getPlayerUseCase, GetPlayer.RequestValues(playerID)).map { it.player }
    }

    fun registerFormErrorResult(): LiveData<FormErrorResult> {
        return errorResult
    }
}