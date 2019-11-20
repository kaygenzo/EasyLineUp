package com.telen.easylineup.domain

import android.net.Uri
import com.telen.easylineup.UseCase
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.PlayerDao
import io.reactivex.Single
import java.lang.Exception

class SavePlayer(val dao: PlayerDao): UseCase<SavePlayer.RequestValues, SavePlayer.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return when {
            requestValues.name.isNullOrBlank() -> Single.error(Exception("Name is empty"))
            requestValues.shirtNumber == null -> Single.error(Exception("Shirt number is empty"))
            requestValues.licenseNumber == null -> Single.error(Exception("License number is  empty"))
            else -> {
                val player = Player(id = requestValues.playerID, teamId = requestValues.teamID, name = requestValues.name.trim(), shirtNumber = requestValues.shirtNumber,
                        licenseNumber = requestValues.licenseNumber, image = requestValues.imageUri?.toString(), positions = requestValues.positions)

                val task = if(player.id == 0L) {
                    dao.insertPlayer(player)
                }
                else {
                    dao.updatePlayer(player)
                }

                task.andThen(Single.just(ResponseValue()))
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