package com.telen.easylineup.domain

import android.net.Uri
import com.telen.easylineup.UseCase
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.PlayerDao

class SavePlayer(val dao: PlayerDao): UseCase<SavePlayer.RequestValues, SavePlayer.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let {
            when {
                it.name.isNullOrBlank() -> mUseCaseCallback?.onError()
                it.shirtNumber == null -> mUseCaseCallback?.onError()
                it.licenseNumber == null -> mUseCaseCallback?.onError()
                else -> {
                    val player = Player(id = it.playerID, teamId = it.teamID, name = it.name.trim(), shirtNumber = it.shirtNumber,
                            licenseNumber = it.licenseNumber, image = it.imageUri?.toString(), positions = it.positions)

                    val task = if(player.id == 0L) {
                        dao.insertPlayer(player)
                    }
                    else {
                        dao.updatePlayer(player)
                    }

                    task.subscribe({
                        mUseCaseCallback?.onSuccess(ResponseValue())
                    }, {
                        mUseCaseCallback?.onError()
                    })
                }
            }
        }
    }

    inner class RequestValues(val playerID: Long,
                              val teamID: Long,
                              val name: String?,
                              val shirtNumber: Int?,
                              val licenseNumber: Long?,
                              val imageUri: Uri?,
                              val positions: Int
    ): UseCase.RequestValues

    inner class ResponseValue: UseCase.ResponseValue
}